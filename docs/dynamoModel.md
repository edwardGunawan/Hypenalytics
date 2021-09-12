# Dynamo DB Data Model

## Table Name
picasso-hypenalytics-inventory-{env}


## Regions
- us-east-1

## Partition Key
- Single Table Design
- Each value will have a PK and SK, and a GSIPK, GSISK

## Entity

## Metadata

|Name|Description|Type|
|----|------|----|
| userId (PK)  | user Id| string |
| inventoryId (SK) | inventory ID | string |
| itemId | item's Id | string |
| category | category type | string |
| priceBuy | buying price | string |
| priceSold | price that is sold | string |
| lastUpdated | timestamp for last updated | string |

## Listing
|Name|Description|Type|
|----|------|----|
| userId (PK)  | user Id| string |
| inventoryId#Platform (SK) | inventory ID | string |
| platform | platform name | string |
| priceAsk | Map[Size -> Map[Price, Quantity]] | List |


Query All the Inventory:
- using `begins_with(inventoryId#)` will just fetch the Listing
- - using `begins_with(inventoryId)` will just fetch both the listing and the metadata
