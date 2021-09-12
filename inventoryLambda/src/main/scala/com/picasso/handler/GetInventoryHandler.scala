package com.picasso.handler

import cats.effect
import cats.effect.IO
import cats.effect.kernel.{Resource, Sync}
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}

import java.io.{InputStream, OutputStream}
import io.circe.parser._
import io.circe.syntax._
import cats.implicits._
import com.picasso.db.{InventoryDB, InventoryDBImpl}
import com.picasso.domain.AppError
import com.picasso.domain.inventory.{GetInventoryResponse, Pagination}
import com.picasso.domain.lambda.{LambdaRequestWithBody, LambdaRequestWithoutBody, LambdaResponse}
import com.picasso.handler.GetInventoryHandler.{LambdaRequest, PathParameters}
import com.picasso.model.InventoryModel
import com.picasso.model.InventoryModel.{Listing, Metadata}
import io.circe.DecodingFailure
import io.circe.generic.JsonCodec
import meteor.Expression
import meteor.SortKeyQuery.BeginsWith
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import cats.effect.unsafe.implicits._
import com.picasso.config.Config

class GetInventoryHandler extends RequestStreamHandler {

  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {

    val resp = for {
      config <- Config.container.load[IO]
      inputString <- IO(scala.io.Source.fromInputStream(input).mkString)
      _ <- IO(println(s"input string $inputString"))
      getInventoryResponse <- GetInventoryHandler.resource(config).use { flow =>
        flow.run(inputString)
      }
    } yield (getInventoryResponse)

    resp.attempt.unsafeRunSync match {
      case Left(ex) =>
        println(s"Get error $ex")
        output.write(ex.lambdaErrHandle.getBytes)
      case Right(value) =>
        output.write(LambdaResponse(body = value.asJson.noSpaces, 200).asJson.noSpaces.getBytes)
    }
  }
}

case class GetInventoryFlow[F[_]: Sync](inventoryDB: InventoryDB[F], tableName: String) {
  private val S = effect.Sync[F]

  def run(inputString: String): F[GetInventoryResponse] =
    for {
      lambdaRequest <- S.fromEither(parse(inputString))
      _ <- S.delay(println(s"Lambda request ${lambdaRequest.asJson.spaces2} getting inventory"))
      inventoriesModel <- getInventory(lambdaRequest.pathParameters)
      _ <- S.delay(println(s"inventoryModel ${inventoriesModel}"))
      getInventoryResponse <- S.fromOption(
        transform(inventoryModels = inventoriesModel),
        AppError.notFoundError(
          s"The inventory cannot be transform to response (either the listings, metadata or both doesn't exist)"
        )
      )
      _ <- S.delay(println(s"getInventoryResponse ${getInventoryResponse}"))
    } yield (getInventoryResponse)

  def parse(inputString: String): Either[AppError, GetInventoryHandler.LambdaRequest] =
    decode[LambdaRequest](inputString).leftMap { throwable =>
      AppError.serializationError(throwable.getMessage)
    }

  def getInventory(pathParameters: PathParameters): F[List[InventoryModel]] =
    inventoryDB
      .listInventoryItems(
        tableName = tableName,
        userId = pathParameters.userId,
        sortKeyQuery = BeginsWith(s"${pathParameters.inventoryId}"),
        filter = Expression.empty
      )
      .compile
      .toList

  def transform(inventoryModels: List[InventoryModel]): Option[GetInventoryResponse] = {

    val (metadata, listings) = inventoryModels.partition {
      case m: Metadata => true
      case l: Listing => false
    }

    val metadataType = metadata.headOption.map {
      case m: Metadata => m
    }

    val listingType = listings.map {
      case l: Listing => l
    }

    val getInventoryResponse = (metadataType, Option(listingType)).mapN {
      case (Metadata(userId, inventoryId, itemId, priceBuy, priceSold, lastUpdated, category), ls) =>
        GetInventoryResponse(
          userId = userId,
          inventoryId = inventoryId,
          priceBuy = priceBuy,
          priceSold = priceSold,
          lastUpdated = lastUpdated,
          itemId = itemId,
          listings = ls,
          pagination = Pagination.empty,
          category = category
        )
    }

    getInventoryResponse
  }
}

object GetInventoryHandler {

  @JsonCodec
  case class PathParameters(userId: String, inventoryId: String)

  @JsonCodec
  case class LambdaRequest(pathParameters: PathParameters)

  def resource(config: Config): Resource[IO, GetInventoryFlow[IO]] =
    for {
      ddbClient <- Resource.fromAutoCloseable[IO, DynamoDbAsyncClient](IO.delay(DynamoDbAsyncClient.builder.build))
      inventoryDB = InventoryDBImpl[IO](ddbClient)
      getInventoryFlow = GetInventoryFlow[IO](inventoryDB, tableName = config.tableName)
    } yield getInventoryFlow

}
