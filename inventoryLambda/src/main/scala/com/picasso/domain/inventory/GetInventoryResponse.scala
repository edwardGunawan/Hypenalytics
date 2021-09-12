package com.picasso.domain.inventory

import com.picasso.model.InventoryModel
import com.picasso.model.InventoryModel.Listing
import io.circe.generic.JsonCodec

import java.time.Instant

@JsonCodec
case class GetInventoryResponse(
  userId: String,
  inventoryId: String,
  priceBuy: String,
  priceSold: Option[String],
  lastUpdated: Instant,
  itemId: String,
  listings: List[Listing],
  pagination: Pagination,
  category: String
)
