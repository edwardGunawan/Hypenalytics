package com.picasso.db

import cats.effect.kernel.Async
import com.picasso.model.InventoryModel
import meteor.{DynamoDbType, Expression, KeyDef, Query, SortKeyQuery}
import meteor.api.hi.CompositeTable
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.ReturnValue

import java.time.Instant
import cats.implicits._
import meteor.syntax._

case class InventoryDB[F[_]: Async](dynamoDbAsyncClient: DynamoDbAsyncClient) extends Repository[F, InventoryModel] {

  override def getItem(
    tableName: String,
    sk: String,
    pk: String
  ): F[Option[InventoryModel]] = {
    val tableSrc = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef[String](attributeName = "PK", DynamoDbType.S),
      sortKeyDef = KeyDef(attributeName = "SK", DynamoDbType.S),
      jClient = dynamoDbAsyncClient
    )
    tableSrc
      .get[InventoryModel](partitionKey = pk, sortKey = sk, consistentRead = true) // the partitionKey if getting inventory is userId, if getting PriceAsk is inventoryId (change it to having PK to userId, SK to split between inventoryId)

  }

  override def insertItem(
    tableName: String,
    inventoryModel: InventoryModel
  ): F[Unit] = {
    val tableSrc = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef[String](attributeName = "PK", DynamoDbType.S),
      sortKeyDef = KeyDef(attributeName = "SK", DynamoDbType.S),
      jClient = dynamoDbAsyncClient
    )

    tableSrc.put[InventoryModel](inventoryModel)
  }

  override def listItems(
    tableName: String,
    pk: String,
    sortKeyQuery: SortKeyQuery[String],
    filter: Expression
  ): fs2.Stream[F, InventoryModel] = {
    val tableSrc = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef(attributeName = "PK", DynamoDbType.S),
      jClient = dynamoDbAsyncClient,
      sortKeyDef = KeyDef(attributeName = "SK", DynamoDbType.S)
    )

    val query = Query[String, String](partitionKey = pk, sortKeyQuery = sortKeyQuery, filter = filter)

    tableSrc
      .retrieve[InventoryModel](query = query, consistentRead = true, limit = Integer.MAX_VALUE) // for now, when items gets really big, we figure how to do real ddb pagination

  }

  override def updateItem(
    tableName: String,
    sk: String,
    pk: String,
    expression: Expression
  ): F[Option[InventoryModel]] = {
    val tblSrc = CompositeTable[F, String, String](
      tableName = tableName,
      partitionKeyDef = KeyDef(attributeName = "PK", DynamoDbType.S),
      sortKeyDef = KeyDef(attributeName = "SK", DynamoDbType.S),
      jClient = dynamoDbAsyncClient
    )

    tblSrc.update[InventoryModel](
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
      jClient = dynamoDbAsyncClient
    )

    tblSrc.delete(partitionKey = pk, sortKey = sk)
  }
}
