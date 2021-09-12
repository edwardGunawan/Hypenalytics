package com.picasso.util

import com.picasso.validation.Validation
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import cats.implicits._

class ValidationSpec extends AnyWordSpec with Matchers {
  "Validation" should {
    "able to validate decimal points" in {
      Validation.isNumerical[Either[Throwable, *]]("12.00") must equal(Right("12.00"))

    }

    "able to validate not decimal points" in {
      Validation.isNumerical[Either[Throwable, *]]("Hello") mustBe a[Left[_, _]]

    }
  }

}
