package com.picasso.domain.inventory

import cats.ApplicativeError
import com.picasso.model.InventoryModel.{Listing, PriceAsk}
import com.picasso.validation.Validation
import io.circe.generic.JsonCodec
import cats.implicits._
import com.picasso.model.Platform

@JsonCodec
case class UpdateInventoryRequest(priceBuy: Option[String], priceSold: Option[String], listings: List[UpdateListing])

@JsonCodec
case class UpdateListing(platform: Platform, priceAsk: Map[String, PriceAsk])

object UpdateListing {
  implicit def validateInstance[F[_]: ApplicativeError[*[_], Throwable]] = new Validation[F, UpdateListing] {
    override def validate(model: UpdateListing): F[UpdateListing] =
      model.priceAsk.values.toList
        .traverse { pAsk =>
          Validation.validate[F, PriceAsk](pAsk)
        }
        .map { _ =>
          model
        }
  }
}

object UpdateInventoryRequest {
  implicit def validationInstance[F[_]: ApplicativeError[*[_], Throwable]] = new Validation[F, UpdateInventoryRequest] {
    private val appErr = ApplicativeError[F, Throwable]
    override def validate(model: UpdateInventoryRequest): F[UpdateInventoryRequest] =
      if (model.priceBuy.isEmpty && model.priceSold.isEmpty && model.listings.isEmpty)
        appErr.raiseError(Validation.ValidationError(msg = "Input value cannot be all empty."))
      else {
        (model.priceBuy.traverse { pb =>
          Validation.isNumerical[F](pb)
        }, model.priceSold.traverse { ps =>
          Validation.isNumerical[F](ps)
        }, model.listings.traverse(Validation.validate[F, UpdateListing])).mapN { (_, _, _) =>
          model
        }
      }
  }
}
