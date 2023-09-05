package com.picasso.schema

import com.picasso.fixture.fixture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PriceEngineSchemaSpec extends AnyWordSpec with Matchers with fixture {
  "PriceEngineSchema" should {
    "transform price schema from dynamoValue to Price" in {
      PriceEngineSchema.priceSchema.write(price) must equal(Right(priceDynamoValue))

    }

    "transform price schema from Price to dynamoValue" in {
      PriceEngineSchema.priceSchema.read(priceDynamoValue) must equal(Right(price))
    }

    "transform priceEngineSchema schema from dynamoValue to PriceEngine model" in {
      PriceEngineSchema.priceEngineSchema.read(priceEngineDynamoValue) must equal(Right(priceEngineModel))
    }

    "transform priceEngineSchema schema from Price to dynamoValue" in {
      PriceEngineSchema.priceEngineSchema.write(priceEngineModel) must equal(Right(priceEngineDynamoValue))
    }

  }

}
