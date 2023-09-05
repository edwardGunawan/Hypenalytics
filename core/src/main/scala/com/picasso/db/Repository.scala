package com.picasso.db

import meteor.{DynamoDbType, Expression, KeyDef, Query, SortKeyQuery}

trait Repository[F[_], M] {
  def getItem(tableName: String, sk: String, pk: String): F[Option[M]]

  def insertItem(
    tableName: String,
    model: M
  ): F[Unit]

  def listItems(
    tableName: String,
    pk: String,
    sortKeyQuery: SortKeyQuery[String],
    filter: Expression
  ): fs2.Stream[F, M]

  def updateItem(tableName: String, sk: String, pk: String, expression: Expression): F[Option[M]]

  def deleteItem(tableName: String, pk: String, sk: String): F[Unit]
}
