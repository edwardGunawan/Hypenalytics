package com.picasso.domain

import io.circe.generic.JsonCodec

sealed trait AppError extends Exception {
  def code: Int
  def msg: String

  override def getMessage: String = s"$msg error message: ${super.getMessage}"
}

object AppError {

  @JsonCodec
  case class InternalServerError(code: Int = 500, msg: String) extends Exception(msg) with AppError

  @JsonCodec
  case class ClientError(code: Int, msg: String) extends Exception(msg) with AppError

  def notFoundError(msg: String): AppError = ClientError(404, msg)
  def validationError(msg: String): AppError = ClientError(400, msg)
  def serializationError(msg: String): AppError = ClientError(code = 400, msg = msg)
}
