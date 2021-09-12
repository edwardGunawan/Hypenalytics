package com.picasso.domain.inventory

import cats.ApplicativeError
import com.picasso.model.InventoryModel.{Listing, PriceAsk}
import com.picasso.validation.Validation
import io.circe.generic.JsonCodec
import cats.implicits._
import com.picasso.model.Platform

import java.time.Instant

@JsonCodec
case class InsertInventoryRequest(
  priceBuy: String,
  priceSold: Option[String],
  listings: List[InsertListing],
  itemId: String,
  category: String
)

object InsertInventoryRequest {

  implicit def validateInstance[F[_]: ApplicativeError[*[_], Throwable]] = new Validation[F, InsertInventoryRequest] {
    override def validate(model: InsertInventoryRequest): F[InsertInventoryRequest] =
      (
        Validation.isNumerical[F](model.priceBuy),
        model.priceSold.traverse { Validation.isNumerical[F] },
        model.listings.traverse { Validation.validate[F, InsertListing] }
      ).mapN { (_, _, _) =>
        model
      }

  }
}
