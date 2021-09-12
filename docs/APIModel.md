# Inventory API Model

## Inventory
| Name | Description | Type | Possible Value|
| ---- | ---- | --- | --- |
| inventoryId | inventory identifier | String | "949445ca-7122-420a-9fc9-635a6fc0fa02" |
| priceBuy <br>**required** | buying price | String | "10.00" |
| priceSold | price that is sold | String  | "12.00" |
| category | category items | String | "Shoes", "Clothes" |
| lastUpdated <br>**required** | String | "2021-09-08T22:20:12.636Z" |
| itemId <br>**required** | the detail item Id of this inventory | String | "949445ca-7122-420a-9fc9-635a6fc0fa02" |
| listings | listings in this inventory | Array[[Listing](#Listing)]  |
| category | category type | String | "shoes" |

## Listing
| Name | Description | Type | Possible Value|
| ---- | ---- | --- | --- |
| platform <br>**required** | name of the platform | String | "GOAT" |
| listOfPriceAsks  | price asks (size -> PriceAsk) | Map[String,[PriceAsks](#PriceAsks)] | |

## UpdateListing
| Name | Description | Type | Possible Value|
| ---- | ---- | --- | --- |
| platform <br>**required** | name of the platform | String | "GOAT" |
| listOfPriceAsks  | price asks (size -> PriceAsk) | Map[String, [PriceAsks](#PriceAsks)] | |

## PriceAsks
| Name | Description | Type | Possible Value|
| ---- | ---- | --- | --- |
| size <br>**required** | size of the inventoryItem | String | "8" or "M" |
| price <br>**required** | price of the inventoryItem | String | "90.99" |
| quantity <br>**required** | quantity of the inventoryItem | String | 8 |

## UpdateInventoryRequest
| Name | Description |Type | Possible Value |
| --- | --- | --- | --- |
| priceBuy | buying price | String | "80.00" |
| priceSold | price that is sold | String | "90.00" |
| listings | Listings | Array[[UpdateListing](#UpdateListing)] | |

JSON Example:
```json
{
  "priceBuy" : null,
  "priceSold" : null,
  "listings" : [
    {
      "platform" : "GOAT",
      "priceAsk" : {
        "8" : {
          "price" : "12.00",
          "quantity" : 2
        }
      }
    }
  ]
}
```

## InsertInventoryRequest
| Name | Description |Type | Possible Value |
| --- | --- | --- | --- |
| priceBuy <br>**required** | buying price | String | "80.00" |
| priceSold | price that is sold | String | "90.00" |
| listings | Listings | Array[[Listings](#Listing)] | |
| itemId | item's Id | String | "123" |
| category | Category Type | String | "shoes" |

JSON Example:
```json
{
  "priceBuy" : "12.00",
  "priceSold" : null,
  "listings" : [
    {
      "userId" : "1",
      "platform" : "GOAT",
      "lstOfPriceAsk" : {
        "8" : {
          "price" : "12.00",
          "quantity" : 2
        }
      },
      "lastUpdated" : "2021-09-08T22:20:12.636Z"
    }
  ],
  "itemId" : "123",
  "category" : "shoes"
}
```


## UpdateInventoryResponse (Equivalent to get inventory Get Inventory Response, set unit for now)
| Name | Description |Type | Possible Value |
| --- | --- | --- | --- |
| userId <br>**required** | user identifier | String  |  "949445ca-7122-420a-9fc9-635a6fc0fa02"|
| inventoryId <br>**required** | inventory identifier | String | "949445ca-7122-420a-9fc9-635a6fc0fa02" |
| inventory | inventory details | [Inventory](#Inventory) | |  


## GetInventoryResponse
| Name | Description |Type | Possible Value |
| --- | --- | --- | --- |
| userId <br>**required** | user identifier | String  | "949445ca-7122-420a-9fc9-635a6fc0fa02" |
| inventory <br>**required** | inventory details | [Inventory](#Inventory) | |

JSON Example:
```json
{
  "userId" : "1",
  "inventoryId" : "2",
  "priceBuy" : "12.00",
  "priceSold" : "12.00",
  "lastUpdated" : "2021-09-08T22:20:12.636Z",
  "itemId" : "12",
  "listings" : [
    {
      "userId" : "1",
      "inventoryId" : "2",
      "platform" : "GOAT",
      "lstOfPriceAsk" : {
        "8" : {
          "price" : "12.00",
          "quantity" : 2
        }
      },
      "lastUpdated" : "2021-09-08T22:20:12.636Z"
    }
  ],
  "pagination" : {
    "previousToken" : "123",
    "nextToken" : "123"
  },
  "category" : "shoes"
}

```

## ListInventoryResponse
| Name | Description |Type | Possible Value |
| --- | --- | --- | --- |
| userId <br>**required** | user identifier | String | "949445ca-7122-420a-9fc9-635a6fc0fa02" |
| inventories <br>**required** | a list of inventories | Array[[Inventory](#Inventory)] | |

JSON Example:
```json 
{
  "userId" : "1",
  "inventories" : [
    {
      "inventoryId" : "2",
      "priceBuy" : "123",
      "priceSold" : "123.00",
      "lastUpdated" : "2021-09-08T22:20:12.636Z",
      "itemId" : "12",
      "platform" : "GOAT",
      "priceAsks" : {
        "8" : {
          "price" : "12.00",
          "quantity" : 2
        }
      }
    }
  ],
  "pagination" : {
    "previousToken" : "123",
    "nextToken" : "123"
  }
}
```
