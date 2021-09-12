package com.picasso.domain.lambda

import io.circe.generic.JsonCodec

@JsonCodec
case class LambdaResponse(body: String, statusCode: Int, isBase64Encoded: Boolean = false)
