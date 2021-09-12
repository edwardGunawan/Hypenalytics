# Inventory REST API Contract

## Overview

### Version Information
*version* : v1

## Transport Method
- REST


### Get Inventory

#### Description
Get an item in the inventoryDB

```
GET /{{version}}/users/${userId}/inventories/${inventoryId}
```

#### Responses
| Http Code | Description | Schema |
| --- | --- | ---|
| 200 | Successfully Output | |
| 400 | Not Found | [ServiceError](#ServiceError)
| 500 | Internal Server Error | [ServiceError](#ServiceError)

### Insert Inventory Item

#### Description
Insert an inventory items in the user name
```
POST /{{version}}/users/{{userId}}/inventories
```

#### Request
[InsertInventoryRequest](./APIModel.md/#InsertInventoryRequest)

#### Responses
| Http Code | Description | Schema |
| --- | --- | ---|
| 201 | Successfully Created | [GetInventoryResponse](../APIModel.md/#GetInventoryResponse) |
| 500 | Internal Server Error | [ServiceError](#ServiceError)

### List Inventory Items

#### Description
List all items in the inventory DB associated with the userId

```
GET /{{version}}/users/{{userId}}/inventories
```

#### Parameters
| Type| Parameter | Required? |Description | Schema |
| ---|---|---|---| ---|
| **Header**| limit | optional | maximum size to return | string (number) |
| **Header** | page_token | optional |  next page token | string |



Sample:
```
?limit=10&page_token=24387
```

#### Responses
| Http Code | Description | Schema |
| --- | --- | ---|
| 200 | Successfully Output | |
| 400 | Not Found | [ServiceError](#ServiceError)
| 500 | Internal Server Error | [ServiceError](#ServiceError)

### Update an Inventory Item
```
PATCH /{{version}}/users/{{usersId}}/inventories/{{inventoryId}}
```
#### Description
Update an item in the inventoryDB associated with the userId and inventoryID

#### Request Body
[RequestBody](./APIModel.md/#UpdateInventoryRequest)

#### Responses
| Http Code | Description | Schema |
| --- | --- | ---|
| 200 | Successfully Output | |
| 400 | Not Found | [ServiceError](#ServiceError)
| 500 | Internal Server Error | [ServiceError](#ServiceError)

### Delete an Inventory Item
```
DELETE /{{version}}/users/{{usersId}}/inventories/{{inventoryId}}[Optional: /platform/{{platform}}]

```

#### Responses
| Http Code | Description | Schema |
| --- | --- | ---|
| 204 | Successfully Deleted | |
| 400 | Not Found | [ServiceError](#ServiceError)
| 500 | Internal Server Error | [ServiceError](#ServiceError)

#### Description
Delete an item in the inventoryDB associated with the userId and the inventoryId


### ServiceError
|Name|Type|
|---|---|
| **id** <br>**required** | String|
| **message** <br>**required** | String|
| **code** <br>**required** | Int|