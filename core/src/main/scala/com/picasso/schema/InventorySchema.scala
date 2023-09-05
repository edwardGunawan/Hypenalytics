package com.picasso.schema

import dynosaur.{DynamoValue, Schema}
import cats.implicits._
import com.picasso.model.InventoryModel.{Listing, Metadata, PriceAsk}
import com.picasso.model.{InventoryModel, Platform}

import java.time.Instant

object InventorySchema {

  lazy val priceAskSchema: Schema[PriceAsk] = Schema.record[PriceAsk] { field =>
    (field("price", _.price), field("quantity", _.quantity)).mapN { (price: String, quantity: Int) =>
      PriceAsk(price = price, quantity = quantity)
    }
  }

  lazy val listingSchema: Schema[Listing] = Schema.record[Listing] { field =>
    (
      field("PK", _.userId),
      field("SK", _.sk), // this will be the one that we decoded as an SK, also when schema transform to attributeValue they will also look at this to transform to SK. Therefore, this should be here as a placeholder. Although we are not really using it in the Listing Model
      field("inventoryId", _.inventoryId),
      field("platform", _.platform)(platformSchema),
      field("lstOfPrice", _.lstOfPriceAsk)(priceAskSchema.asMap),
      field("lastUpdated", _.lastUpdated)(schemaInstant)
    ).mapN {
      (
        userId: String,
        sk: String,
        inventoryId: String,
        platform: Platform,
        lstOfPrice: Map[String, PriceAsk],
        lastUpdated: Instant
      ) =>
        Listing(
          userId = userId,
          inventoryId = inventoryId,
          platform = platform,
          lstOfPriceAsk = lstOfPrice,
          lastUpdated = lastUpdated
        )
    }
  }

  lazy val metadataModelSchema: Schema[Metadata] = Schema.record[Metadata] { field =>
    (
      field("PK", _.userId),
      field("SK", _.inventoryId),
      field("itemId", _.itemName),
      field("priceBuy", _.priceBuy),
      field.opt("priceSold", _.priceSold),
      field("category", _.category),
      field("lastUpdated", _.lastUpdated)(schemaInstant)
    ).mapN {
      (
        userId: String,
        inventoryId: String,
        itemId: String,
        priceBuy: String,
        priceSold: Option[String],
        category: String,
        lastUpdated: Instant
      ) =>
        Metadata(
          userId = userId,
          inventoryId = inventoryId,
          itemName = itemId,
          priceBuy = priceBuy,
          priceSold = priceSold,
          lastUpdated = lastUpdated,
          category = category
        )
    }

  }

  lazy val inventorySchema: Schema[InventoryModel] = Schema.oneOf[InventoryModel] { alt =>
    alt(metadataModelSchema) |+| alt(listingSchema)
  }

}
