package com.picasso.domain.lambda

import cats.ApplicativeError
import com.picasso.validation.Validation
import io.circe.generic.JsonCodec
import cats.implicits._

@JsonCodec
case class LambdaRequestWithBody[B](
  headers: Option[Map[String, String]],
  queryStringParameters: Option[Map[String, String]],
  pathParameters: Option[Map[String, String]],
  body: Option[B]
)

object LambdaRequestWithBody {
  implicit def validateInstance[F[_]: ApplicativeError[*[_], Throwable], B: Validation[F, *]]: Validation[
    F,
    LambdaRequestWithBody[B]
  ] = new Validation[F, LambdaRequestWithBody[B]] {
    override def validate(model: LambdaRequestWithBody[B]): F[LambdaRequestWithBody[B]] =
      if (model.body.isEmpty)
        Validation.ValidationError(msg = "request must have a body").raiseError[F, LambdaRequestWithBody[B]]
      else
        model.body
          .traverse { bod =>
            Validation.validate[F, B](bod)
          }
          .map { maybeBod =>
            LambdaRequestWithBody[B](
              headers = model.headers,
              pathParameters = model.pathParameters,
              queryStringParameters = model.queryStringParameters,
              body = maybeBod
            )
          }

  }
}
