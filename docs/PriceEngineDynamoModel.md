# Price Engine Dynamo DB Data Model

## Table Name
picasso-hypenalytics-price-engine-{env}


## Regions
- us-east-1

## Partition Key
- Single Table Design
- Each value will have a PK and SK, and a GSIPK, GSISK

## Entity

## Price Engine

|Name|Description|Type|
| ---- | ------ | ---- |
| itemId (base 64 encode itemName) (PK) | Item's Id | String |
| lastInserted (SK) | Last time it is inserted to the field | String |
| imageLink | link to the image of the item | String |
| make | the make of the item | String | 
| colorway | the color of the item | String |
| retailPrice | the retail price of the item | String |
| releaseDate | The release date of the item | String |
| resellLink | the link for resale at GOAT, StockX | Map[`Platform`, String] |
| thumbnailUrl | thumbnail url | String |
| price | price of the item | Map[String, `Price`] |
| brand | the name of the brand | String |
| styleId | the id of the style | String |