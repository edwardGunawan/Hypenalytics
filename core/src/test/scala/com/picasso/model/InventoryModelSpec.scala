package com.picasso.model

import com.picasso.model.Platform.GOAT
import com.picasso.model.Platform.StockX
import io.circe.Json
import io.circe.syntax._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class InventoryModelSpec extends AnyWordSpec with Matchers {
  "InventoryModelSpec" should {
    "platform parsing should work" in {
      Platform.parse("GOAT") must equal(Some(GOAT))
      Platform.parse("stockX") must equal(Some(StockX))
      Platform.parse("somethinElse") must equal(None)
    }

    "platform JSON circe parser" in {
      (GOAT: Platform).asJson must equal(Json.fromString("GOAT"))
      (StockX: Platform).asJson must equal(Json.fromString("StockX"))
    }
  }

}
