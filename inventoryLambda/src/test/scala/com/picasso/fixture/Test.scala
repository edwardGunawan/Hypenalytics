package com.picasso.fixture

import com.picasso.domain.inventory.{InsertInventoryRequest, InsertInventoryRequestSpec, UpdateInventoryRequest}
import com.picasso.handler.InsertInventoryHandler.PathParameters
import io.circe.generic.JsonCodec
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import io.circe.syntax._
import io.circe.parser._

class Test extends AnyWordSpec with Matchers with fixture {
  "test" should {
    "test inputString" in {
      val res =
        """{
        |    "version": "1.0",
        |    "resource": "/users/{userId}/inventories",
        |    "path": "/v1/users/1/inventories",
        |    "httpMethod": "POST",
        |    "headers": {
        |        "Content-Length": "341",
        |        "Content-Type": "application/json",
        |        "Host": "n2gti2n4a3.execute-api.us-east-1.amazonaws.com",
        |        "Postman-Token": "8aa35c46-df9b-407d-aefd-946dbfd96c7a",
        |        "User-Agent": "PostmanRuntime/7.28.4",
        |        "X-Amzn-Trace-Id": "Root=1-613d0e66-33a0353b187916c54dbb176e",
        |        "X-Forwarded-For": "76.95.2.30",
        |        "X-Forwarded-Port": "443",
        |        "X-Forwarded-Proto": "https",
        |        "accept": "*/*",
        |        "accept-encoding": "gzip, deflate, br",
        |        "cache-control": "no-cache"
        |    },
        |    "multiValueHeaders": {
        |        "Content-Length": [
        |            "341"
        |        ],
        |        "Content-Type": [
        |            "application/json"
        |        ],
        |        "Host": [
        |            "n2gti2n4a3.execute-api.us-east-1.amazonaws.com"
        |        ],
        |        "Postman-Token": [
        |            "8aa35c46-df9b-407d-aefd-946dbfd96c7a"
        |        ],
        |        "User-Agent": [
        |            "PostmanRuntime/7.28.4"
        |        ],
        |        "X-Amzn-Trace-Id": [
        |            "Root=1-613d0e66-33a0353b187916c54dbb176e"
        |        ],
        |        "X-Forwarded-For": [
        |            "76.95.2.30"
        |        ],
        |        "X-Forwarded-Port": [
        |            "443"
        |        ],
        |        "X-Forwarded-Proto": [
        |            "https"
        |        ],
        |        "accept": [
        |            "*/*"
        |        ],
        |        "accept-encoding": [
        |            "gzip, deflate, br"
        |        ],
        |        "cache-control": [
        |            "no-cache"
        |        ]
        |    },
        |    "queryStringParameters": null,
        |    "multiValueQueryStringParameters": null,
        |    "requestContext": {
        |        "accountId": "303997205411",
        |        "apiId": "n2gti2n4a3",
        |        "domainName": "n2gti2n4a3.execute-api.us-east-1.amazonaws.com",
        |        "domainPrefix": "n2gti2n4a3",
        |        "extendedRequestId": "Fg8wBjeaIAMEJeQ=",
        |        "httpMethod": "POST",
        |        "identity": {
        |            "accessKey": null,
        |            "accountId": null,
        |            "caller": null,
        |            "cognitoAmr": null,
        |            "cognitoAuthenticationProvider": null,
        |            "cognitoAuthenticationType": null,
        |            "cognitoIdentityId": null,
        |            "cognitoIdentityPoolId": null,
        |            "principalOrgId": null,
        |            "sourceIp": "76.95.2.30",
        |            "user": null,
        |            "userAgent": "PostmanRuntime/7.28.4",
        |            "userArn": null
        |        },
        |        "path": "/v1/users/1/inventories",
        |        "protocol": "HTTP/1.1",
        |        "requestId": "Fg8wBjeaIAMEJeQ=",
        |        "requestTime": "11/Sep/2021:20:15:34 +0000",
        |        "requestTimeEpoch": 1631391334389,
        |        "resourceId": "POST /users/{userId}/inventories",
        |        "resourcePath": "/users/{userId}/inventories",
        |        "stage": "v1"
        |    },
        |    "pathParameters": {
        |        "userId": "1"
        |    },
        |    "stageVariables": null,
        |    "body": "{\n  \"priceBuy\" : \"12.00\",\n  \"priceSold\" : null,\n  \"listings\" : [\n    {\n      \"userId\" : \"1\",\n      \"platform\" : \"StockX\",\n      \"lstOfPriceAsk\" : {\n        \"8\" : {\n          \"price\" : \"13.00\",\n          \"quantity\" : 2\n        }\n      },\n      \"lastUpdated\" : \"2021-09-08T22:20:12.636Z\"\n    }\n  ],\n  \"itemId\" : \"123\",\n  \"category\" : \"shoes\"\n}",
        |    "isBase64Encoded": false
        |}""".stripMargin

      @JsonCodec
      case class LambdaRequest(pathParameters: PathParameters, body: String)

      println(decode[LambdaRequest](res).flatMap { lambda =>
        decode[InsertInventoryRequest](lambda.body)
      })
    }
//    val query =
//      "{\n  \"priceBuy\" : \"12.00\",\n  \"priceSold\" : null,\n  \"listings\" : [\n    {\n      \"userId\" : \"1\",\n      \"platform\" : \"StockX\",\n      \"lstOfPriceAsk\" : {\n        \"8\" : {\n          \"price\" : \"13.00\",\n          \"quantity\" : 2\n        }\n      },\n      \"lastUpdated\" : \"2021-09-08T22:20:12.636Z\"\n    }\n  ],\n  \"itemId\" : \"123\",\n  \"category\" : \"shoes\"\n}"
//    println(decode[InsertInventoryRequest](query))
//    println(insertInventoryRequest.asJson.spaces2)

    "test2" in {
      println(updateInventoryRequest.asJson.spaces2)
    }
  }

  ""

}
