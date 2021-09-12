package com.picasso.domain.inventory

import cats.ApplicativeError
import com.picasso.model.InventoryModel.PriceAsk
import com.picasso.model.Platform
import com.picasso.validation.Validation
import io.circe.generic.JsonCodec
import cats.implicits._
import java.time.Instant

@JsonCodec
case class InsertListing(userId: String, platform: Platform, lstOfPriceAsk: Map[String, PriceAsk], lastUpdated: Instant)

object InsertListing {
  implicit def validateInstance[F[_]: ApplicativeError[*[_], Throwable]] = new Validation[F, InsertListing] {
    override def validate(model: InsertListing): F[InsertListing] =
      model.lstOfPriceAsk.values.toList
        .traverse { p =>
          Validation.validate[F, PriceAsk](p)
        }
        .map { _ =>
          model
        }
  }
}
