package com.picasso.handler

import cats.Parallel
import cats.data.OptionT
import cats.effect.IO
import cats.effect.kernel.{Async, Resource, Sync}
import com.amazonaws.services.lambda.runtime.{Context, RequestStreamHandler}
import com.picasso.config.Config
import com.picasso.db.{InventoryDB, InventoryDBImpl}
import com.picasso.domain.inventory.{GetInventoryResponse, UpdateInventoryRequest, UpdateListing}
import com.picasso.model.InventoryModel
import io.circe.generic.JsonCodec
import meteor.Expression
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import java.io.{InputStream, OutputStream}
import meteor.syntax._
import io.circe.syntax._
import io.circe.parser._
import cats.implicits._
import cats.effect.unsafe.implicits._
import com.picasso.domain.AppError
import com.picasso.domain.lambda.LambdaResponse
import com.picasso.handler.UpdateInventoryHandler.{LambdaRequest, resource}
import com.picasso.model.InventoryModel.{Listing, Metadata}
import com.picasso.validation.Validation
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException

class UpdateInventoryHandler extends RequestStreamHandler {
  override def handleRequest(input: InputStream, output: OutputStream, context: Context): Unit = {
    val resp = for {
      config <- Config.container.load[IO]
      inputString <- IO(scala.io.Source.fromInputStream(input).mkString)
      _ <- IO(println(s"InputString is ${inputString}"))
      _ <- resource[IO](config = config).use { flow =>
        flow.run(inputString)
      }
    } yield ()

    resp.attempt.unsafeRunSync match {
      case Right(unit) => output.write(LambdaResponse(body = "[Success]", statusCode = 200).asJson.noSpaces.getBytes)
      case Left(ex) =>
        println(s"Update Err Msg: $ex")
        output.write(ex.lambdaErrHandle.getBytes)
    }
  }
}

case class UpdateInventoryFlow[F[_]: Sync: Parallel](inventoryDB: InventoryDB[F], tableName: String) {
  private val S = Sync[F]

  def run(inputString: String): F[Unit] =
    (for {
      tup <- S.fromEither(parse(inputString))
      (lambdaReq, updateInventoryRequest) = tup
      validatedModel <- Validation.validate[F, UpdateInventoryRequest](updateInventoryRequest)
      inventoryId = lambdaReq.pathParameters.inventoryId
      userId = lambdaReq.pathParameters.userId
      _ <- S.ifM(S.delay(isMetadataNeededUpdate(validatedModel)))(
        updateMetadata(
          inventoryId = inventoryId,
          userId = userId,
          priceSold = validatedModel.priceSold,
          priceBuy = validatedModel.priceBuy
        ) *> S.delay(println("Finish updating metadata")),
        S.delay(println("Metadata doesn't need to be updated"))
      )
      _ <- S.ifM(S.delay(isListingNeedUpdate(validatedModel)))(
        updateListings(validatedModel.listings, inventoryId = inventoryId, userId = userId) *> S.delay(
          println("Finish updating listing")
        ),
        S.delay(println("Listing doesn't need to be updated"))
      )
    } yield ()).adaptErr {
      case err: DynamoDbException =>
        AppError.InternalServerError(msg = err.getMessage)
      case err => err
    }

  def parse(inputString: String): Either[Throwable, (LambdaRequest, UpdateInventoryRequest)] =
    (for {
      lambdaReq <- decode[LambdaRequest](inputString)
      updateInventoryReq <- decode[UpdateInventoryRequest](lambdaReq.body)
    } yield ((lambdaReq, updateInventoryReq))).leftMap { throwable =>
      AppError.serializationError(throwable.getMessage)
    }

  def isMetadataNeededUpdate(updateInventoryRequest: UpdateInventoryRequest): Boolean =
    updateInventoryRequest.priceBuy.nonEmpty || updateInventoryRequest.priceSold.nonEmpty

  def isListingNeedUpdate(updateInventoryRequest: UpdateInventoryRequest): Boolean =
    updateInventoryRequest.listings.nonEmpty

  def updateMetadata(
    inventoryId: String,
    userId: String,
    priceSold: Option[String],
    priceBuy: Option[String]
  ): F[Option[InventoryModel.Metadata]] = {
    val priceSoldExpression = priceSold
      .map { p =>
        Expression(
          expression = s"#n = :v",
          attributeNames = Map("#n" -> "priceSold"),
          attributeValues = Map(":v" -> p.asAttributeValue)
        )
      }
      .getOrElse(Expression.empty)

    val priceBuyExpression = priceBuy
      .map { p =>
        Expression(
          expression = "#pb = :priceBuy",
          attributeNames = Map("#pb" -> "priceBuy"),
          attributeValues = Map(":priceBuy" -> p.asAttributeValue)
        )
      }
      .getOrElse(Expression.empty)
    val combinedExpression = priceBuyExpression.add(priceSoldExpression)

    println(s"combinedExpression ${combinedExpression}")
    OptionT(
      inventoryDB.updateInventoryItem(
        tableName = tableName,
        sk = inventoryId,
        userId = userId,
        expression = combinedExpression.copy(
          expression = s"SET ${combinedExpression.expression}"
        )
      )
    ).map {
      case m: Metadata => m
    }.value
  }

  def updateListings(
    listings: List[UpdateListing],
    inventoryId: String,
    userId: String
  ): F[Option[List[InventoryModel.Listing]]] = {
    val fOptLst = listings
      .parTraverse { updateListing =>
        updateListing.priceAsk.toList.traverse {
          case (size, priceAsk) =>
            val exp = Expression(
              expression = s"SET #lstOfPrice.#size = :lstPriceVal",
              attributeNames = Map("#lstOfPrice" -> "lstOfPrice", "#size" -> size),
              attributeValues = Map(":lstPriceVal" -> priceAsk.asAttributeValue)
            )
            inventoryDB.updateInventoryItem(
              tableName = tableName,
              sk = s"$inventoryId#${updateListing.platform}",
              userId = userId,
              expression = exp
            )
        }
      }
      .map(_.flatten.sequence)

    OptionT(fOptLst).map { lst =>
      lst.map {
        case l: Listing => l
      }
    }.value

  }

}

object UpdateInventoryHandler {

  def resource[F[_]: Async: Parallel](config: Config): Resource[F, UpdateInventoryFlow[F]] =
    Resource
      .fromAutoCloseable {
        Async[F].delay {
          DynamoDbAsyncClient.builder().build()
        }
      }
      .map { ddbClient =>
        val inventoryDB = InventoryDBImpl[F](ddbClient)
        UpdateInventoryFlow[F](inventoryDB, config.tableName)
      }

  @JsonCodec
  case class PathParameters(userId: String, inventoryId: String)

  @JsonCodec
  case class LambdaRequest(pathParameters: PathParameters, body: String)
}
