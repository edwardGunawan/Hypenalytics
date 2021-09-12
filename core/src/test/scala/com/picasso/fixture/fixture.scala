package com.picasso.fixture

import dynosaur.DynamoValue

import java.time.Instant
import scala.collection.convert.AsJavaConverters
import com.picasso.model.InventoryModel.{Metadata, Listing, PriceAsk}
import com.picasso.model.Platform

trait fixture extends AsJavaConverters {
  lazy val metadata = Metadata(
    userId = "1",
    inventoryId = "2",
    itemId = "2",
    priceBuy = "12.00",
    priceSold = Some("12.00"),
    lastUpdated = Instant.parse("2021-09-08T22:20:12.636Z"),
    category = "shoes"
  )

  lazy val priceAsk = PriceAsk(price = "12.00", quantity = 2)

  lazy val listings = Listing(
    userId = "1",
    inventoryId = "2",
    platform = Platform.GOAT,
    lstOfPriceAsk = Map("8" -> priceAsk),
    lastUpdated = Instant.parse("2021-09-08T22:20:12.636Z")
  )

  lazy val priceAskDynamoValue = DynamoValue.m(
    "price" -> DynamoValue.s(priceAsk.price),
    "quantity" -> DynamoValue.n(priceAsk.quantity)
  )

  lazy val listingDynamoValue = DynamoValue.m(
    "inventoryId" -> DynamoValue.s(listings.inventoryId),
    "lstOfPrice" -> DynamoValue.m(Map("8" -> priceAskDynamoValue)),
    "lastUpdated" -> DynamoValue.s(listings.lastUpdated.toString),
    "PK" -> DynamoValue.s(listings.userId),
    "SK" -> DynamoValue.s(s"${listings.inventoryId}#${listings.platform}"),
    "platform" -> DynamoValue.s(listings.platform.toString)
  )

  lazy val metadataDynamoValue = DynamoValue.m(
    Map(
      "priceBuy" -> DynamoValue.s(metadata.priceBuy),
      "priceSold" -> DynamoValue.s(metadata.priceSold.get),
      "itemId" -> DynamoValue.s(metadata.itemId),
      "SK" -> DynamoValue.s(metadata.inventoryId),
      "PK" -> DynamoValue.s(metadata.userId),
      "lastUpdated" -> DynamoValue.s(metadata.lastUpdated.toString),
      "category" -> DynamoValue.s(metadata.category)
    )
  )

}
