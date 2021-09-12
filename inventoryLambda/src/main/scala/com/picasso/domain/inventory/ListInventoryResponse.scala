package com.picasso.domain.inventory

import com.picasso.model.InventoryModel.{Listing, PriceAsk}
import com.picasso.model.Platform
import io.circe.generic.JsonCodec

import java.time.Instant

@JsonCodec
case class ListInventoryResponse(userId: String, inventories: List[Inventory], pagination: Pagination)

@JsonCodec
case class Inventory(
  inventoryId: String,
  priceBuy: String,
  priceSold: Option[String],
  lastUpdated: Instant,
  itemId: String,
  platform: Platform,
  priceAsks: Map[String, PriceAsk]
)
