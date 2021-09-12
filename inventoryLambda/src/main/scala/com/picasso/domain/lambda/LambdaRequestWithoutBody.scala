package com.picasso.domain.lambda

import io.circe.generic.JsonCodec

@JsonCodec
case class LambdaRequestWithoutBody (headers: Option[Map[String, String]],
                                     queryStringParameters: Option[Map[String, String]],
                                     pathParameters: Option[Map[String,String]])
