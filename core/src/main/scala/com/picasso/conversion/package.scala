package com.picasso

import dynosaur.{DynamoValue, Schema}
import meteor.codec.Codec
import meteor.errors
import meteor.errors.DecoderError
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import cats.implicits._

package object conversion {
  implicit def schemaToCodec[A](schema: Schema[A]): Codec[A] = new Codec[A] {
    override def write(a: A): AttributeValue =
      schema.write(a).fold(err => throw err, _.value)

    override def read(av: AttributeValue): Either[errors.DecoderError, A] =
      schema.read(DynamoValue(av)).leftMap { schemErr =>
        DecoderError(message = schemErr.message, cause = schemErr.getCause.some)
      }

  }
}
