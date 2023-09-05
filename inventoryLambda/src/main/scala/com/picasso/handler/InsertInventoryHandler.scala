package com.picasso.handler

import cats.effect.kernel.{Resource, Sync}
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.picasso.db.{Repository, InventoryDB}
import com.picasso.domain.inventory.{InsertInventoryRequest, InsertInventoryResponse}
import io.circe.generic.JsonCodec
import com.picasso.domain.AppError
import com.picasso.model.InventoryModel
import com.picasso.model.InventoryModel.{Listing, Metadata}

import java.io.{InputStream, OutputStream}
import java.time.Instant
import java.util.UUID
import InsertInventoryHandler._
import cats.effect.{Async, IO}
import com.picasso.config.Config
import com.picasso.validation.Validation
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import cats.effect.unsafe.implicits._
import com.picasso.domain.lambda.LambdaResponse
import cats.implicits._
import io.circe.parser.{decode, _}
import io.circe.syntax._

class InsertInventoryHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val resp = for {
      config <- Config.container.load[IO]
      inputString <- IO(scala.io.Source.fromInputStream(input).mkString)
      _ <- IO(println(s"input string $inputString"))
      insertInventoryResponse <- InsertInventoryHandler.resource[IO](config).use { flow =>
        flow.run(inputString)
      }
    } yield (insertInventoryResponse)

    resp.attempt.unsafeRunSync() match {
      case Left(ex) =>
        println(s"Insert Error msg: $ex")
        output.write(ex.lambdaErrHandle.getBytes)
      case Right(value) =>
        output.write(LambdaResponse(statusCode = 201, body = value.asJson.noSpaces).asJson.noSpaces.getBytes)
    }
  }
}

case class InsertInventoryFlow[F[_]: Sync](inventoryDB: Repository[F, InventoryModel], tableName: String) {
  private val S = Sync[F]

  def run(inputString: String): F[InsertInventoryResponse] =
    for {
      tup <- S.fromEither(parse(inputString))
      (lambdaReq, insertInventoryReq) = tup
      validatedBody <- Validation.validate[F, InsertInventoryRequest](insertInventoryReq)
      inventoryId <- S.delay(UUID.randomUUID.toString)
      now = Instant.now
      inventoriesModel <- S.delay(
        transform(
          insertInventoryRequest = validatedBody,
          pathParameters = lambdaReq.pathParameters,
          inventoryId = inventoryId,
          now = now
        )
      )

      _ <- insertDB(inventoriesModel)
      resp = InsertInventoryResponse(inventoryId)
    } yield (resp)

  def parse(inputString: String): Either[Throwable, (LambdaRequest, InsertInventoryRequest)] =
    (for {
      lambdaReq <- decode[LambdaRequest](inputString)
      insertInventoryReq <- decode[InsertInventoryRequest](lambdaReq.body)
    } yield ((lambdaReq, insertInventoryReq))).leftMap { throwable =>
      AppError.serializationError(throwable.getMessage)
    }

  def insertDB(inventoriesModel: List[InventoryModel]): F[Unit] =
    inventoriesModel.traverse { inventoryModel =>
      inventoryDB.insertItem(tableName = tableName, model = inventoryModel)
    } *> S.unit

  def transform(
    insertInventoryRequest: InsertInventoryRequest,
    pathParameters: PathParameters,
    inventoryId: String,
    now: Instant
  ): List[InventoryModel] = {

    val listings = insertInventoryRequest.listings.map { insertListing =>
      Listing(
        userId = pathParameters.userId,
        inventoryId = inventoryId,
        platform = insertListing.platform,
        lstOfPriceAsk = insertListing.lstOfPriceAsk,
        lastUpdated = now
      )
    }

    val metadata = Metadata(
      userId = pathParameters.userId,
      inventoryId = inventoryId,
      itemName = insertInventoryRequest.itemId,
      priceBuy = insertInventoryRequest.priceBuy,
      priceSold = insertInventoryRequest.priceSold,
      lastUpdated = now,
      category = insertInventoryRequest.category
    )

    metadata :: listings

  }
}

object InsertInventoryHandler {

  def resource[F[_]: Async](config: Config): Resource[F, InsertInventoryFlow[F]] =
    Resource
      .fromAutoCloseable {
        Sync[F].delay {
          DynamoDbAsyncClient.builder.build
        }
      }
      .map { ddbClient =>
        val inventoryDB = InventoryDB[F](dynamoDbAsyncClient = ddbClient)
        InsertInventoryFlow[F](inventoryDB = inventoryDB, tableName = config.tableName)
      }

  @JsonCodec
  case class PathParameters(userId: String)

  @JsonCodec
  case class LambdaRequest(pathParameters: PathParameters, body: String)
}
