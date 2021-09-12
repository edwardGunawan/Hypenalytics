package com.picasso.fixture

import com.picasso.model.InventoryModel.{Listing, PriceAsk}
import com.picasso.domain.inventory.{GetInventoryResponse, InsertInventoryRequest, InsertInventoryRequestSpec, InsertListing, Inventory, ListInventoryResponse, Pagination, UpdateInventoryRequest, UpdateListing}

import java.time._
import com.picasso.model.Platform
import com.picasso.model.Platform.GOAT

trait fixture {
  lazy val getInventoryResponse = GetInventoryResponse(
    userId = "1",
    inventoryId = "2",
    priceBuy = "12.00",
    priceSold = Some("12.00"),
    lastUpdated = Instant.parse("2021-09-08T22:20:12.636Z"),
    itemId = "12",
    listings = List(listings),
    pagination = pagination,
    category = "shoes"
  )

  lazy val insertInventoryRequest = InsertInventoryRequest(
    priceBuy = "12.00",
    priceSold = None,
    listings = List(insertListing),
    itemId = "123",
    category = "shoes"
  )

  lazy val insertListing = InsertListing(
    userId = "1",
    platform = Platform.GOAT,
    lstOfPriceAsk = Map("8" -> priceAsk),
    lastUpdated = Instant.parse("2021-09-08T22:20:12.636Z")
  )

  lazy val listInventoryResponse =
    ListInventoryResponse(userId = "1", inventories = List(inventory), pagination = pagination)

  lazy val inventory = Inventory(
    inventoryId = "2",
    priceBuy = "123",
    priceSold = Some("123.00"),
    lastUpdated = Instant.parse("2021-09-08T22:20:12.636Z"),
    itemId = "12",
    platform = Platform.GOAT,
    priceAsks = Map("8" -> priceAsk)
  )

  lazy val pagination = Pagination(previousToken = Some("123"), nextToken = Some("123"))

  lazy val priceAsk = PriceAsk(price = "12.00", quantity = 2)

  lazy val listings = Listing(
    userId = "1",
    inventoryId = "2",
    platform = Platform.GOAT,
    lstOfPriceAsk = Map("8" -> priceAsk),
    lastUpdated = Instant.parse("2021-09-08T22:20:12.636Z")
  )

  lazy val updateInventoryRequest =
    UpdateInventoryRequest(priceBuy = None, priceSold = None, listings = List(updateListing))

  lazy val updateListing = UpdateListing(platform = GOAT, priceAsk = Map("8" -> priceAsk))

}
