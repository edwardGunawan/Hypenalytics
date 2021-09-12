package com.picasso.domain.inventory

import cats.ApplicativeError
import com.picasso.validation.Validation
import io.circe.generic.JsonCodec

/*
  Number of items that is dropped:
  0 will be None
  nextToken will be the limit it will just add 0 + limit.
 */
@JsonCodec
case class Pagination(previousToken: Option[String], nextToken: Option[String]) {

  def fromPreviousToken: Option[String] = previousToken.map { prevToken =>
    new String(java.util.Base64.getDecoder.decode(prevToken))
  }
}

object Pagination {
  def empty: Pagination = Pagination(previousToken = None, nextToken = None)
}
