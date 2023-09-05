package com.picasso.fixture

import dynosaur.DynamoValue

import java.time.Instant
import scala.collection.convert.AsJavaConverters
import com.picasso.model.InventoryModel.{Listing, Metadata, PriceAsk}
import com.picasso.model.Platform.{GOAT, StockX}
import com.picasso.model.{Platform, Price, PriceEngineModel}

trait fixture extends AsJavaConverters {
  lazy val metadata = Metadata(
    userId = "1",
    inventoryId = "2",
    itemName = "2",
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
      "itemId" -> DynamoValue.s(metadata.itemName),
      "SK" -> DynamoValue.s(metadata.inventoryId),
      "PK" -> DynamoValue.s(metadata.userId),
      "lastUpdated" -> DynamoValue.s(metadata.lastUpdated.toString),
      "category" -> DynamoValue.s(metadata.category)
    )
  )

  lazy val priceEngineModel = PriceEngineModel(
    itemName = "jordan 5",
    lastInserted = Instant.parse("2021-09-08T22:20:12.636Z"),
    imageLink = "www.google.com",
    make = "2012",
    colorway = "dark",
    retailPrice = "12.00",
    releaseDate = "2021-09-08T22:20:12.636Z",
    resellLink = Map(
      GOAT -> "https://resellLink",
      StockX -> "https://resellLink"
    ),
    thumbnailUrl = "thumbnail.url",
    price = Map(
      "9" -> price
    ),
    brand = "addidas",
    styleId = "555088-701"
  )

  lazy val priceEngineDynamoValue = DynamoValue.m(
    Map(
      "itemName" -> DynamoValue.s(priceEngineModel.itemName),
      "releaseDate" -> DynamoValue.s(priceEngineModel.releaseDate),
      "lastInserted" -> DynamoValue.s(priceEngineModel.lastInserted.toString),
      "imageLink" -> DynamoValue.s(priceEngineModel.imageLink),
      "make" -> DynamoValue.s(priceEngineModel.make),
      "colorway" -> DynamoValue.s(priceEngineModel.colorway),
      "retailPrice" -> DynamoValue.s(priceEngineModel.retailPrice),
      "resellLink" -> DynamoValue.m(priceEngineModel.resellLink.map {
        case (platform, str) => platform.toString -> DynamoValue.s(str)
      }),
      "thumbnailUrl" -> DynamoValue.s(priceEngineModel.thumbnailUrl),
      "price" -> DynamoValue.m("9" -> priceDynamoValue),
      "brand" -> DynamoValue.s(priceEngineModel.brand),
      "styleId" -> DynamoValue.s(priceEngineModel.styleId)
    )
  )

  lazy val priceDynamoValue = DynamoValue.m(
    Map(
      "lowestAsk" -> DynamoValue.s(price.lowestAsk),
      "topBid" -> DynamoValue.s(price.topBid),
      "lastSell" -> DynamoValue.s(price.lastSell)
    )
  )

  lazy val price = Price(lowestAsk = "12.00", topBid = "12.11", lastSell = "12.00")

}
