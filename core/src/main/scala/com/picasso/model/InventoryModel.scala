package com.picasso.model

import cats.{Applicative, ApplicativeError}
import com.picasso.conversion._
import com.picasso.schema.InventorySchema
import com.picasso.validation.Validation
import io.circe.generic.JsonCodec
import io.circe.generic.extras.semiauto._
import io.circe.{Decoder, Encoder}
import meteor.codec.Codec

import java.time.Instant
import cats.implicits._
sealed trait InventoryModel {
  def pk: String
  def sk: String
  def userId: String
  def inventoryId: String
  def lastUpdated: Instant

}

object InventoryModel {

  @JsonCodec
  case class PriceAsk(price: String, quantity: Int)

  object PriceAsk {
    implicit val codec: Codec[PriceAsk] = InventorySchema.priceAskSchema

    implicit def validateInstance[F[_]: ApplicativeError[*[_], Throwable]] = new Validation[F, PriceAsk] {
      override def validate(model: PriceAsk): F[PriceAsk] = Validation.isNumerical[F](model.price).map { _ =>
        model
      }
    }
  }

  @JsonCodec
  case class Listing(
    userId: String,
    inventoryId: String,
    platform: Platform,
    lstOfPriceAsk: Map[String, PriceAsk],
    lastUpdated: Instant
  ) extends InventoryModel {
    // we want the PK and SK but don't want to have the hassle to parse the SK. Therefore,keep the SK as a placeholder and have an additional inventoryId as an attribute
    override def pk: String = userId

    override def sk: String = s"${inventoryId}#${platform}"
  }

  object Listing {

    implicit val codec: Codec[Listing] = InventorySchema.listingSchema

    implicit def validateInstance[F[_]: ApplicativeError[*[_], Throwable]] = new Validation[F, Listing] {
      override def validate(model: Listing): F[Listing] = {
        model.lstOfPriceAsk.values.toList
          .traverse { priceAsk =>
            Validation.validate[F, PriceAsk](priceAsk)
          }
          .map { _ =>
            model
          }
      }
    }

  }

  case class Metadata(
    userId: String,
    inventoryId: String,
    itemId: String,
    priceBuy: String,
    priceSold: Option[String],
    lastUpdated: Instant,
    category: String
  ) extends InventoryModel {
    override def pk: String = userId

    override def sk: String = inventoryId
  }

  object Metadata {
    implicit val codec: Codec[Metadata] = InventorySchema.metadataModelSchema
  }

  implicit val codec: Codec[InventoryModel] = InventorySchema.inventorySchema

}
