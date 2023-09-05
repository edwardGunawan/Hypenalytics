package com.picasso.db

import cats.effect.kernel.Async
import com.picasso.model.{InventoryModel, PriceEngineModel}
import meteor.api.hi.CompositeTable
import meteor.{DynamoDbType, Expression, KeyDef, Query, SortKeyQuery}
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.ReturnValue
import meteor.syntax._
import java.time.Instant

case class PriceEngineDB[F[_]: Async](client: DynamoDbAsyncClient) extends Repository[F, PriceEngineModel] {
  override def getItem(tableName: String, sk: String, pk: String): F[Option[PriceEngineModel]] = {

    val compositeTable = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef[String](attributeName = "PK", attributeType = DynamoDbType.S),
      sortKeyDef = KeyDef[String](attributeName = "SK", attributeType = DynamoDbType.S),
      jClient = client
    )

    compositeTable.get[PriceEngineModel](partitionKey = pk, sortKey = sk, consistentRead = true)
  }

  override def insertItem(tableName: String, model: PriceEngineModel): F[Unit] = {
    val compositeTable = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef[String](attributeName = "PK", attributeType = DynamoDbType.S),
      sortKeyDef = KeyDef[String](attributeName = "SK", attributeType = DynamoDbType.S),
      jClient = client
    )

    compositeTable.put[PriceEngineModel](t = model)
  }

  override def listItems(
    tableName: String,
    pk: String,
    sortKeyQuery: SortKeyQuery[String],
    filter: Expression
  ): fs2.Stream[F, PriceEngineModel] = {
    val compositeTable = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef[String](attributeName = "PK", attributeType = DynamoDbType.S),
      sortKeyDef = KeyDef[String](attributeName = "SK", attributeType = DynamoDbType.S),
      jClient = client
    )

    val query = Query[String, String](partitionKey = pk, sortKeyQuery = sortKeyQuery, filter = filter)

    compositeTable.retrieve[PriceEngineModel](query = query, consistentRead = true, limit = Integer.MAX_VALUE)

  }

  override def updateItem(
    tableName: String,
    sk: String,
    pk: String,
    expression: Expression
  ): F[Option[PriceEngineModel]] = {
    val tblSrc = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef(attributeName = "PK", DynamoDbType.S),
      sortKeyDef = KeyDef(attributeName = "SK", DynamoDbType.S),
      jClient = client
    )

    tblSrc.update[PriceEngineModel](
      partitionKey = pk,
      sortKey = sk,
      update = expression.copy(
        expression = expression.expression + ", " + "#lastUpdated = :lastUpdatedVal",
        attributeNames = expression.attributeNames + ("#lastUpdated" -> "lastUpdated"),
        attributeValues = expression.attributeValues + (":lastUpdatedVal" -> Instant.now.toString.asAttributeValue)
      ),
      returnValue = ReturnValue.ALL_NEW,
      condition = Expression.empty
    )
  }

  override def deleteItem(tableName: String, pk: String, sk: String): F[Unit] = {
    val tblSrc = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef(attributeName = "PK", DynamoDbType.S),
      sortKeyDef = KeyDef(attributeName = "SK", DynamoDbType.S),
      jClient = client
    )

    tblSrc.delete(partitionKey = pk, sortKey = sk)
  }
}
