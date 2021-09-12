package com.picasso

import cats.effect.{ExitCode, IO, IOApp}
import com.picasso.db.InventoryDBImpl
import com.picasso.model.InventoryModel.{Listing, Metadata, PriceAsk}
import com.picasso.model.InventoryModel.{Listing, Metadata}
import com.picasso.model.Platform.GOAT
import meteor.Expression
import meteor.SortKeyQuery.BeginsWith
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient

import java.time.Instant
import java.util.UUID
import cats.implicits._
import meteor.syntax._

object Main extends IOApp {

  def genMetadata: Metadata = Metadata(
    inventoryId = UUID.randomUUID().toString,
    userId = "1",
    itemId = UUID.randomUUID().toString,
    priceBuy = "12",
    priceSold = None,
    lastUpdated = Instant.now,
    category = "shoes"
  )

  def genListings(inventoryId: String, userId: String): Listing =
    Listing(
      userId = userId,
      inventoryId = inventoryId,
      platform = GOAT,
      lstOfPriceAsk = Map("8" -> PriceAsk(price = "12.00", quantity = 3)),
      lastUpdated = Instant.now
    )

  val dynamoDbAsyncClient = DynamoDbAsyncClient.builder.build

  val tableName = "test-inventory"

  override def run(args: List[String]): IO[ExitCode] =
    for {
      metadatas <- IO((0 to 4).toList.map(_ => genMetadata))
      listings <- IO(metadatas.map(metadata => genListings(metadata.inventoryId, metadata.userId)))

      inventoryDB = InventoryDBImpl[IO](dynamoDbAsyncClient)

      _ <- IO(println("Inserting metadatas"))
      _ <- metadatas.traverse(
        metadata => inventoryDB.insertInventoryItem(tableName = tableName, inventoryModel = metadata)
      )
      randomInventoryId = metadatas.head.inventoryId
      randomUserId = metadatas.head.userId

      _ <- IO(println(s"randomeInventoryId = $randomInventoryId randomUserId = $randomUserId"))

      _ <- IO(println("Checking if metadatas can be retrieved from DDB"))

      inventoryModel <- inventoryDB.getInventoryItem(tableName = tableName, sk = randomInventoryId, pk = randomUserId)

      _ <- IO(println(s"inventoryModel: $inventoryModel"))

      _ <- IO(println("inserting listings"))

      _ <- listings.traverse(
        listing => inventoryDB.insertInventoryItem(tableName = tableName, inventoryModel = listing)
      )

      _ <- IO(println("Finish inserting listings ..."))

//      inventoriesStreams <- inventoryDB
//        .listInventoryItems(
//          tableName = tableName,
//          userId = "1",
//          sortKeyQuery = BeginsWith("949445ca-7122-420a-9fc9-635a6fc0fa02"),
//          filter = Expression.empty
//        )
//        .compile
//        .toList
//
//      _ <- IO(println(s"inventoryStream  - ${inventoriesStreams}"))
//      exp1 = Expression(
//        expression = "#pb = :priceBuy",
//        attributeNames = Map("#pb" -> "priceBuy"),
//        attributeValues = Map(":priceBuy" -> "13.00".asAttributeValue)
//      )
//      exp2 = Expression(
//        expression = "#ps = :priceSale",
//        attributeNames = Map(
//          "#ps" -> "priceSale"
//        ),
//        attributeValues = Map(":priceSale" -> "22.00".asAttributeValue)
//      )
//      combinedExp = Expression(
//        expression = exp1.expression + ", " + exp2.expression,
//        attributeNames = exp1.attributeNames ++ exp2.attributeNames,
//        attributeValues = exp1.attributeValues ++ exp2.attributeValues
//      )
//
//      _ <- IO(println(s"combinedExp ${combinedExp}"))
//      _ <- inventoryDB
//        .updateInventoryItem(
//          tableName = tableName,
//          sk = "9b5cd407-65c7-4c2a-a1c2-9b905b3bb78e",
//          userId = "1",
//          expression = combinedExp.copy(
//            expression = "SET " + combinedExp.expression
//          )
//        )
//        .handleErrorWith { throwable =>
//          println(s"throwable ${throwable}")
//          IO.raiseError(throwable)
//        }

//      _ <- inventoryDB.deleteInventoryItem(tableName, pk = "1", sk = "011d9b23-d0da-44cd-b71d-441f1148b240#")
//      inventoryModelMaybe <- inventoryDB.getInventoryItem(
//        tableName,
//        sk = "011d9b23-d0da-44cd-b71d-441f1148b240#",
//        pk = "1"
//      )
//      _ <- IO(println(inventoryModelMaybe))

    } yield ExitCode.Success
}
