package com.picasso.domain.inventory

import io.circe.generic.JsonCodec

@JsonCodec
case class InsertInventoryResponse(inventoryId: String)
