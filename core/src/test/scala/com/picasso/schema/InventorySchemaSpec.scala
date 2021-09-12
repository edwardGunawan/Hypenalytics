package com.picasso.schema

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.picasso.fixture.fixture

class InventorySchemaSpec extends AnyWordSpec with Matchers with fixture {
  "InventorySchema" should {
    "convert metadata to dynamo model successfully" in {
      InventorySchema.inventorySchema.write(metadata) must equal(Right(metadataDynamoValue))
    }

    "convert from dynamoValue to metadata model successfully" in {
      InventorySchema.inventorySchema.read(metadataDynamoValue) must equal(Right(metadata))
    }

    "convert listing to dynamo model successfully" in {
      InventorySchema.inventorySchema.write(listings) must equal(Right(listingDynamoValue))
    }

    "convert from dynamoValue to listings model successful" in {
      InventorySchema.inventorySchema.read(listingDynamoValue) must equal(Right(listings))
    }
  }

}
