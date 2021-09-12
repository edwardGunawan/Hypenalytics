package com.picasso

import com.picasso.domain.AppError
import com.picasso.domain.lambda.LambdaResponse
import com.picasso.handler.UpdateInventoryHandler.LambdaRequest
import io.circe.syntax._
import meteor.Expression
package object handler {
  implicit class errOps(err: Throwable) {

    def lambdaErrHandle: String = err match {
      case i @ AppError.InternalServerError(code, _) =>
        LambdaResponse(body = i.asJson.noSpaces, code).asJson.noSpaces
      case c @ AppError.ClientError(code, _) =>
        LambdaResponse(body = c.asJson.noSpaces, code).asJson.noSpaces
      case e =>
        LambdaResponse(body = e.getMessage, statusCode = 500).asJson.noSpaces
    }
  }

  implicit class ExpressionOp(exp1: Expression) {

    def add(exp2: Expression): Expression =
      if (exp1.isEmpty) exp2
      else if (exp2.isEmpty) exp1
      else {
        exp1.copy(
          expression = exp1.expression + ", " + exp2.expression,
          attributeNames = exp1.attributeNames ++ exp2.attributeNames,
          attributeValues = exp1.attributeValues ++ exp2.attributeValues
        )
      }
  }
}
