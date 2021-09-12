package com.picasso.domain.inventory

import com.picasso.fixture.fixture
import com.picasso.validation.Validation
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import cats.implicits._

class InsertInventoryRequestSpec extends AnyWordSpec with Matchers with fixture {
  "InsertInventoryRequest" should {
    "Validate if the value is numeric is correct" in {
      Validation.validate[Either[Throwable, *], InsertInventoryRequest](insertInventoryRequest) must equal(
        Right(insertInventoryRequest)
      )
    }

    "Validate if the value is not numeric" in {
      Validation.validate[Either[Throwable, *], InsertInventoryRequest](insertInventoryRequest.copy(priceBuy = "hello")) mustBe a[
        Left[_, _]
      ]
    }
  }

}
