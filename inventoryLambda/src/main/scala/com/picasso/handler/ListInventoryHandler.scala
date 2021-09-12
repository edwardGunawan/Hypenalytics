package com.picasso.handler

import cats.effect.IO
import cats.effect.kernel.{Async, Resource, Sync}
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.picasso.db.{InventoryDB, InventoryDBImpl}
import com.picasso.domain.inventory.{Inventory, ListInventoryResponse, Pagination}
import com.picasso.handler.ListInventoryHandler.{LambdaRequest, QueryStringParameters}
import io.circe.generic.JsonCodec

import java.io.{InputStream, OutputStream}
import io.circe.syntax._
import io.circe.parser._
import cats.implicits._
import cats.effect.unsafe.implicits._
import com.picasso.config.Config
import com.picasso.domain.AppError
import com.picasso.domain.AppError.{ClientError, InternalServerError}
import com.picasso.domain.lambda.LambdaResponse
import com.picasso.model.InventoryModel
import com.picasso.model.InventoryModel.{Listing, Metadata}
import com.picasso.util.Codec
import meteor.Expression
import meteor.SortKeyQuery.Empty
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

class ListInventoryHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val resp = for {
      config <- Config.container.load[IO]
      inputString <- IO(scala.io.Source.fromInputStream(input).mkString)
      _ <- IO(println(s"input string $inputString"))
      res <- ListInventoryHandler.resource[IO](config).use { lstInventory =>
        lstInventory.run(inputString)
      }
    } yield (res)

    resp.attempt.unsafeRunSync match {
      case Left(ex) =>
        println(s"List Err Message: $ex")
        output.write(ex.lambdaErrHandle.getBytes)
      case Right(value) =>
        output.write(LambdaResponse(body = value.asJson.noSpaces, statusCode = 200).asJson.noSpaces.getBytes)
    }
  }
}

case class ListInventory[F[_]: Sync](inventoryDB: InventoryDB[F], tableName: String) extends Codec {
  private val S = Sync[F]

  type InventoryId = String

  def run(inputString: String): F[ListInventoryResponse] =
    for {
      lambdaReq <- S.fromEither(parse(inputString))
      queryParam <- S.delay(
        lambdaReq.queryStringParameters.getOrElse(QueryStringParameters(limit = None, pageToken = None))
      )
      pageTokenMaybe <- S.fromEither(queryParam.pageToken.traverse { token =>
        pageToken(token)
      })
      groupInventory <- listInventoryDB(
        queryParam.limit,
        pageTokenMaybe,
        userId = lambdaReq.pathParameters.userId
      )

      _ <- S.delay(
        println(
          s"Inventory Model : ${groupInventory} ${groupInventory.length} limit: ${queryParam.limit} - token: ${pageTokenMaybe}"
        )
      )

      pagination = computePagination(pageTokenMaybe, groupInventory.length)

      listInventoryResp <- S.fromOption(
        transform(groupInventory = groupInventory, pagination = pagination),
        AppError.notFoundError(msg = s"There is some problem during the transformation")
      )

    } yield (listInventoryResp)

  def parse(inputString: String): Either[Throwable, LambdaRequest] = decode[LambdaRequest](inputString).leftMap {
    throwable =>
      AppError.serializationError(throwable.getMessage)
  }

  def pageToken(pageToken: String): Either[Throwable, Int] =
    Either.catchOnly[Throwable](decodeBase64(pageToken).toInt).leftMap { throwable =>
      AppError.serializationError(s"Not able to decode the page token ${throwable.getMessage}")
    }

  def listInventoryDB(
    limit: Option[Int],
    pageToken: Option[Int],
    userId: String
  ): F[List[(InventoryId, List[InventoryModel])]] = {
    val stream = inventoryDB
      .listInventoryItems(
        tableName = tableName,
        userId = userId,
        sortKeyQuery = Empty[String],
        filter = Expression.empty
      )

    stream
      .groupAdjacentBy { _.inventoryId }
      .drop(pageToken.getOrElse(0).toLong)
      .take(
        limit
          .fold(100) { i =>
            if (i > 100) 100 else i
          }
          .toLong
      )
      .map {
        case (str, chunks) => str -> chunks.toList
      }
      .compile
      .toList
  }

  def computePagination(pageToken: Option[Int], currIterator: Int): Pagination =
    pageToken
      .map { lastPageIterator =>
        val lastPageToken = encodeBase64(lastPageIterator.toString)
        val nextPageToken = encodeBase64((lastPageIterator + currIterator).toString)
        Pagination(lastPageToken.some, nextPageToken.some)
      }
      .getOrElse {
        val nextPageToken = encodeBase64(currIterator.toString)
        Pagination(none, nextPageToken.some)
      }

  def transform(
    groupInventory: List[(InventoryId, List[InventoryModel])],
    pagination: Pagination
  ): Option[ListInventoryResponse] = {
    val listInventoriesMaybe = groupInventory
      .traverse {
        case (inventoryId, lst) =>
          val (m, l) = lst.partition {
            case m: Metadata => true
            case l: Listing => false
          }

          println(s"m $m and l $l")

          val metadataTypeMaybe = m.headOption.map { case x: Metadata => x }
          val inventoriesMaybe = l.traverse {
            case y: Listing => y.some
            case _ => none
          }
          (metadataTypeMaybe, inventoriesMaybe).mapN { (metadataType, inventories) =>
            inventories.map { i =>
              Inventory(
                inventoryId = i.inventoryId,
                priceBuy = metadataType.priceBuy,
                priceSold = metadataType.priceSold,
                lastUpdated = metadataType.lastUpdated,
                itemId = metadataType.itemId,
                platform = i.platform,
                priceAsks = i.lstOfPriceAsk
              )
            }
          }
      }
      .map(_.flatten)

    val userIdMaybe = for {
      inventoryModelHead <- groupInventory.headOption
      inventoryModel <- inventoryModelHead._2.headOption
    } yield (inventoryModel.userId)

    for {
      i <- listInventoriesMaybe
      userId <- userIdMaybe
      listInventoryResp = ListInventoryResponse(userId = userId, inventories = i, pagination = pagination)
    } yield (listInventoryResp)

  }
}

object ListInventoryHandler {

  def resource[F[_]: Async](config: Config): Resource[F, ListInventory[F]] =
    Resource
      .fromAutoCloseable(
        Async[F].delay(
          DynamoDbAsyncClient.builder.build
        )
      )
      .map { ddbClient =>
        val inventoryDB = InventoryDBImpl[F](ddbClient)
        ListInventory(inventoryDB, tableName = config.tableName)
      }

  @JsonCodec
  case class LambdaRequest(pathParameters: PathParameters, queryStringParameters: Option[QueryStringParameters])

  @JsonCodec
  case class QueryStringParameters(limit: Option[Int], pageToken: Option[String])

  @JsonCodec
  case class PathParameters(userId: String)
}
