package com.picasso.model

import com.picasso.schema.InventorySchema
import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto.{deriveEnumerationDecoder, deriveEnumerationEncoder}
import meteor.codec.Codec
import com.picasso.conversion._

sealed trait Platform

object Platform {

  case object GOAT extends Platform
  case object StockX extends Platform

  def parse(str: String): Option[Platform] = str.trim.toLowerCase match {
    case "goat" => Some(GOAT)
    case "stockx" => Some(StockX)
    case _ => None
  }

  implicit val codec: Codec[Platform] = InventorySchema.platformSchema

  implicit val encoder: Encoder[Platform] = deriveEnumerationEncoder[Platform]
  implicit val decoder: Decoder[Platform] = deriveEnumerationDecoder[Platform]

}
