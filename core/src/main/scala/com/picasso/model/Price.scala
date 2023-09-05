package com.picasso.model

import com.picasso.schema.PriceEngineSchema
import io.circe.generic.JsonCodec
import meteor.codec.Codec
import com.picasso.conversion._

@JsonCodec
case class Price(lowestAsk: String, topBid: String, lastSell: String)

object Price {
  implicit val codec: Codec[Price] = PriceEngineSchema.priceSchema
}
