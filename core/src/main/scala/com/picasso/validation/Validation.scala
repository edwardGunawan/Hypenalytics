package com.picasso.validation

import cats.ApplicativeError

trait Validation[F[_], A] {
  def validate(model: A): F[A]
}

object Validation {
  case class ValidationError(msg: String) extends Exception(msg)

  def validate[F[_]: ApplicativeError[*[_], Throwable], A](a: A)(implicit ev: Validation[F, A]) = ev.validate(a)

  def isNumerical[F[_]: ApplicativeError[*[_], Throwable]](str: String): F[String] = {
    val pattern = "^(0|[1-9]\\d*)(\\.\\d+)?$".r
    if (pattern.matches(str)) ApplicativeError[F, Throwable].pure(str)
    else ApplicativeError[F, Throwable].raiseError(ValidationError(msg = s"$str is not numerical"))
  }
}
