package com.picasso.handler

import cats.Parallel
import cats.effect.IO
import cats.effect.kernel.{Async, Resource, Sync}
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import io.circe.generic.JsonCodec

import java.io.{InputStream, OutputStream}
import io.circe.parser._
import io.circe.syntax._
import cats.implicits._
import cats.effect.unsafe.implicits._
import com.picasso.config.Config
import com.picasso.db.{InventoryDB, Repository}
import com.picasso.domain.AppError
import com.picasso.domain.lambda.LambdaResponse
import com.picasso.handler.DeleteInventoryHandler.LambdaRequest
import com.picasso.model.InventoryModel.{Listing, Metadata}
import com.picasso.model.{InventoryModel, Platform}
import io.circe.syntax.EncoderOps
import meteor.Expression
import meteor.SortKeyQuery.BeginsWith
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import javax.naming.ldap.SortKey

class DeleteInventoryHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val resp = for {
      config <- Config.container.load[IO]
      inputString <- IO(scala.io.Source.fromInputStream(input).mkString)
      _ <- IO(println(s"input string ${inputString}"))
      _ <- DeleteInventoryHandler.resource[IO](config).use { flow =>
        flow.run(inputString)
      }
    } yield ()

    resp.attempt.unsafeRunSync match {
      case Left(ex) =>
        println(s"Delete Error: $ex")
        output.write(ex.lambdaErrHandle.getBytes)
      case Right(value) => output.write(LambdaResponse(body = "", statusCode = 204).asJson.noSpaces.getBytes)
    }
  }
}

case class DeleteInventoryFlow[F[_]: Sync: Parallel](inventoryDB: Repository[F, InventoryModel], tableName: String) {
  private val S = Sync[F]

  def run(inputString: String): F[Unit] =
    for {
      lambdaReq <- S.fromEither(parse(inputString))
      userId = lambdaReq.pathParameters.userId
      inventoryId = lambdaReq.pathParameters.inventoryId
      platformMaybe = lambdaReq.pathParameters.platform
      _ <- deleteItem(userId = userId, inventoryId = inventoryId, platformMaybe = platformMaybe)
    } yield ()

  def parse(inputString: String): Either[Throwable, LambdaRequest] = decode[LambdaRequest](inputString).leftMap {
    throwable =>
      AppError.serializationError(throwable.getMessage)
  }

  def deleteItem(userId: String, inventoryId: String, platformMaybe: Option[Platform]): F[Unit] = {
    // list all the inventory if the platform doesn't exist, and then delete all of them
    val ifPlatformNotExist = for {
      inventories <- inventoryDB
        .listItems(
          tableName = tableName,
          pk = userId,
          sortKeyQuery = BeginsWith(s"$inventoryId#"),
          filter = Expression.empty
        )
        .compile
        .toList
      _ <- inventories.parTraverse {
        case Metadata(userId, inventoryId, itemId, priceBuy, priceSold, lastUpdated, category) =>
          inventoryDB.deleteItem(tableName = tableName, pk = userId, sk = inventoryId)
        case Listing(userId, inventoryId, platform, lstOfPriceAsk, lastUpdated) =>
          inventoryDB.deleteItem(tableName = tableName, pk = userId, sk = s"$inventoryId#$platform")
      }
    } yield ()

    platformMaybe.fold(ifPlatformNotExist)(
      s => inventoryDB.deleteItem(tableName = tableName, pk = userId, sk = s"$inventoryId#$s")
    )
  }

}

object DeleteInventoryHandler {

  def resource[F[_]: Async: Parallel](config: Config): Resource[F, DeleteInventoryFlow[F]] =
    Resource
      .fromAutoCloseable(Async[F].delay {
        DynamoDbAsyncClient.builder().build()
      })
      .map { ddbClient =>
        val inventoryDB = InventoryDB[F](ddbClient)
        DeleteInventoryFlow[F](inventoryDB = inventoryDB, tableName = config.tableName)
      }

  @JsonCodec
  case class PathParameters(userId: String, inventoryId: String, platform: Option[Platform])

  @JsonCodec
  case class LambdaRequest(pathParameters: PathParameters)
}
