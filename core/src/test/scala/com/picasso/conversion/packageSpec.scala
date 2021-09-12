package com.picasso.conversion

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import com.picasso.conversion._
import com.picasso.schema.InventorySchema
import meteor.codec.Codec
import com.picasso.model.InventoryModel

class packageSpec extends AnyWordSpec with Matchers {
  "package" should {
    "implicitly derive the conversion to codec" in {
      implicitly[Codec[InventoryModel]](InventorySchema.inventorySchema) mustBe a[Codec[_]]
    }
  }

}
