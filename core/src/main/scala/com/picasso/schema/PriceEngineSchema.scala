package com.picasso.schema

import com.picasso.model.{Platform, Price, PriceEngineModel}
import dynosaur.Schema

import cats.implicits._

object PriceEngineSchema {

  lazy val priceSchema: Schema[Price] = Schema.record[Price] { field =>
    (field("lowestAsk", _.lowestAsk), field("topBid", _.topBid), field("lastSell", _.lastSell)).mapN {
      (lowestAsk: String, topBid: String, lastSell: String) =>
        Price(lowestAsk = lowestAsk, topBid = topBid, lastSell = lastSell)
    }
  }

  lazy val resellLinkSchema: Schema[Map[Platform, String]] = Schema[Map[String, String]].imapErr { f =>
    f.keys.toList
      .traverse(Platform.parse)
      .map { lstPlatform =>
        lstPlatform.zip(f.view.values.toList).toMap
      }
      .toRight(Schema.ReadError(s"$f cannot be decode into Platform "))
  }(r => r.map { case (k, v) => (k.toString -> v) })

  lazy val priceEngineSchema = Schema.record[PriceEngineModel] { field =>
    (
      field("itemName", _.itemName),
      field("lastInserted", _.lastInserted)(schemaInstant),
      field("imageLink", _.imageLink),
      field("make", _.make),
      field("colorway", _.colorway),
      field("releaseDate", _.releaseDate),
      field("resellLink", _.resellLink)(resellLinkSchema),
      field("retailPrice", _.retailPrice),
      field("thumbnailUrl", _.thumbnailUrl),
      field("price", _.price)(priceSchema.asMap),
      field("brand", _.brand),
      field("styleId", _.styleId)
    ).mapN(
      (
        itemName,
        lastInserted,
        imageLink,
        make,
        colorway,
        releaseDate,
        resellLink,
        retailPrice,
        thumbnailUrl,
        price,
        brand,
        styleId
      ) =>
        PriceEngineModel(
          itemName = itemName,
          lastInserted = lastInserted,
          imageLink = imageLink,
          make = make,
          colorway = colorway,
          retailPrice = retailPrice,
          releaseDate = releaseDate,
          resellLink = resellLink,
          thumbnailUrl = thumbnailUrl,
          price = price,
          brand = brand,
          styleId = styleId
        )
    )
  }
}
