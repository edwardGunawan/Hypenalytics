package com.picasso.model

import com.picasso.conversion._
import com.picasso.schema.PriceEngineSchema
import io.circe.{Decoder, Encoder, Json, KeyEncoder}
import io.circe.generic.JsonCodec
import meteor.codec.Codec

import java.time.Instant

@JsonCodec
case class PriceEngineModel(
  itemName: String,
  lastInserted: Instant,
  imageLink: String,
  make: String,
  colorway: String,
  retailPrice: String,
  releaseDate: String,
  resellLink: Map[Platform, String],
  thumbnailUrl: String,
  price: Map[String, Price],
  brand: String,
  styleId: String
)

object PriceEngineModel {
  implicit val codec: Codec[PriceEngineModel] = PriceEngineSchema.priceEngineSchema

}
