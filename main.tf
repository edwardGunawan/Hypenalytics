terraform {
  required_providers {
    aws = { # defining various providers
      source  = "hashicorp/aws"
      version = "~> 3.48.0"
    }
    archive =  {
      source  = "hashicorp/archive"
      version = "~> 2.2.0"
    }

  }
    required_version = "~> 1.0"

}

provider "aws" {
  region = var.aws_region
}

data "archive_file" "lambdaFunctionJar" {
  type = "zip"

  source_dir = "${path.module}/inventoryLambda"
  output_path = "${path.module}/inventoryLambda.zip"

}

resource "aws_dynamodb_table" "inventoryTable" {
  lifecycle {
    prevent_destroy = true # To prevent deleting when doing terraform destroy More info: https://www.terraform.io/docs/language/meta-arguments/lifecycle.html
  }
  name = "picasso-hypenalytics-inventory-table-${var.env}"
  billing_mode = "PROVISIONED"
  read_capacity = 20
  write_capacity = 20

  hash_key = "PK"
  range_key = "SK"

  attribute {
    name = "PK"
    type = "S"
  }

  attribute {
    name = "SK"
    type = "S"
  }


  tags = {
    Name = "hypenalytics-inventory-table"
  }
}

resource "aws_s3_bucket" "lambda_bucket" {
  bucket = "artifactory-${var.aws_region}"
  acl = "private"
  force_destroy = true
}

resource "aws_s3_bucket_object" "s3_object" {
  bucket = aws_s3_bucket.lambda_bucket.id
  key = "hypenalytics/InventoryLambda.jar"
  source = "${path.module}/inventoryLambda/target/scala-2.13/InventoryLambda.jar"

  etag = filemd5("${path.module}/inventoryLambda/target/scala-2.13/InventoryLambda.jar")

}

resource "aws_lambda_function" "get_inventory" {
  function_name = "get-inventory"
  s3_bucket = aws_s3_bucket.lambda_bucket.id
  s3_key = aws_s3_bucket_object.s3_object.key

  runtime = "java8"

  handler = "com.picasso.handler.GetInventoryHandler"

  source_code_hash = data.archive_file.lambdaFunctionJar.output_base64sha256
  timeout = 600
  memory_size = 512

  role = aws_iam_role.lambda_exec.arn

  environment {
    variables = {
      tableName = aws_dynamodb_table.inventoryTable.name
    }
  }
}

resource "aws_lambda_function" "insert_inventory" {
  function_name = "insert-inventory"
  s3_bucket = aws_s3_bucket.lambda_bucket.id
  s3_key = aws_s3_bucket_object.s3_object.key

  runtime = "java8"

  handler = "com.picasso.handler.InsertInventoryHandler"

  source_code_hash = data.archive_file.lambdaFunctionJar.output_base64sha256
  timeout = 600
  memory_size = 512

  role = aws_iam_role.lambda_exec.arn

  environment {
    variables = {
      tableName = aws_dynamodb_table.inventoryTable.name
    }
  }
}


resource "aws_lambda_function" "list_inventory" {
  function_name = "list-inventory"
  s3_bucket = aws_s3_bucket.lambda_bucket.id
  s3_key = aws_s3_bucket_object.s3_object.key

  runtime = "java8"

  handler = "com.picasso.handler.ListInventoryHandler"

  source_code_hash = data.archive_file.lambdaFunctionJar.output_base64sha256
  timeout = 600
  memory_size = 512

  role = aws_iam_role.lambda_exec.arn

  environment {
    variables = {
      tableName = aws_dynamodb_table.inventoryTable.name
    }
  }
}

resource "aws_lambda_function" "update_inventory" {
  function_name = "update-inventory"
  s3_bucket = aws_s3_bucket.lambda_bucket.id
  s3_key = aws_s3_bucket_object.s3_object.key

  runtime = "java8"

  handler = "com.picasso.handler.UpdateInventoryHandler"

  source_code_hash = data.archive_file.lambdaFunctionJar.output_base64sha256
  timeout = 600
  memory_size = 512

  role = aws_iam_role.lambda_exec.arn

  environment {
    variables = {
      tableName = aws_dynamodb_table.inventoryTable.name
    }
  }
}

resource "aws_lambda_function" "delete_inventory" {
  function_name = "delete-inventory"
  s3_bucket = aws_s3_bucket.lambda_bucket.id
  s3_key = aws_s3_bucket_object.s3_object.key

  runtime = "java8"

  handler = "com.picasso.handler.DeleteInventoryHandler"

  source_code_hash = data.archive_file.lambdaFunctionJar.output_base64sha256
  timeout = 600
  memory_size = 512

  role = aws_iam_role.lambda_exec.arn

  environment {
    variables = {
      tableName = aws_dynamodb_table.inventoryTable.name
    }
  }
}


resource "aws_cloudwatch_log_group" "get_inventory_log" {
  name = "/aws/lambda/${aws_lambda_function.get_inventory.function_name}"

  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "list_inventory_log" {
  name = "/aws/lambda/${aws_lambda_function.list_inventory.function_name}"

  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "insert_inventory_log" {
  name = "/aws/lambda/${aws_lambda_function.insert_inventory.function_name}"

  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "update_inventory_log" {
  name = "/aws/lambda/${aws_lambda_function.update_inventory.function_name}"

  retention_in_days = 30
}

resource "aws_cloudwatch_log_group" "delete_inventory_log" {
  name = "/aws/lambda/${aws_lambda_function.delete_inventory.function_name}"

  retention_in_days = 30
}

resource "aws_iam_role" "lambda_exec" {
  name = "picasso-hypenalytics-inventory-lambda-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Sid    = ""
      Principal = {
        Service = "lambda.amazonaws.com"
      }
    }
    ]
  })
}


resource "aws_iam_role_policy" "dynamodb_permission" {
  name = "inventory_ddb_permission"
  role = aws_iam_role.lambda_exec.id # attaching the policy to the role here

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "dynamodb:Query",
        "dynamodb:GetItem",
        "dynamodb:PutItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem"
      ]
      Resource = "arn:aws:dynamodb:${var.aws_region}:${var.aws_account}:table/${aws_dynamodb_table.inventoryTable.name}"
    }]

  })
}

resource "aws_iam_role_policy_attachment" "lambda_policy" {
  role       = aws_iam_role.lambda_exec.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}


resource "aws_apigatewayv2_api" "lambda" {
  name = "serverless_lambda_gw"
  protocol_type = "HTTP"
}

resource "aws_apigatewayv2_stage" "lambda" {
  api_id = aws_apigatewayv2_api.lambda.id
  name = "dev"
  auto_deploy = true

    access_log_settings {
      destination_arn = aws_cloudwatch_log_group.api_gw.arn # this is the api gateway one 
      format = jsonencode({
        requestId = "$context.requestId"
        sourceIp = "$context.identity.sourceIp"
        requestTime = "$context.requestTime"
        caller = "$context.identity.caller",
        protocol = "$context.protocol"
        httpMethod = "$context.httpMethod"
        resourcePath = "$context.resourcePath"
        routeKey = "$context.routeKey"
        status = "$context.status"
        responseLength = "$context.responseLength"
        integrationErrorMessage = "$context.integrationErrorMessage"
        integrationLatency = "$context.integration.latency"
        functionResponseStatus = "$context.integration.status"
        integrationServiceStatus = "$context.integration.integrationStatus"
      }
    )
  }
}

resource "aws_apigatewayv2_integration" "get_inventory" {
  api_id = aws_apigatewayv2_api.lambda.id
  integration_uri = aws_lambda_function.get_inventory.invoke_arn # the lambda function that you want to evoke
  integration_type = "AWS_PROXY"
  integration_method = "POST"
}

resource "aws_apigatewayv2_integration" "insert_inventory" {
  api_id = aws_apigatewayv2_api.lambda.id
  integration_uri = aws_lambda_function.insert_inventory.invoke_arn # the lambda function that you want to evoke
  integration_type = "AWS_PROXY"
  integration_method = "POST"
}

resource "aws_apigatewayv2_integration" "list_inventory" {
  api_id = aws_apigatewayv2_api.lambda.id
  integration_uri = aws_lambda_function.list_inventory.invoke_arn # the lambda function that you want to evoke
  integration_type = "AWS_PROXY"
  integration_method = "POST"
}

resource "aws_apigatewayv2_integration" "update_inventory" {
  api_id = aws_apigatewayv2_api.lambda.id
  integration_uri = aws_lambda_function.update_inventory.invoke_arn # the lambda function that you want to evoke
  integration_type = "AWS_PROXY"
  integration_method = "POST"
}

resource "aws_apigatewayv2_integration" "delete_inventory" {
  api_id = aws_apigatewayv2_api.lambda.id
  integration_uri = aws_lambda_function.delete_inventory.invoke_arn # the lambda function that you want to evoke
  integration_type = "AWS_PROXY"
  integration_method = "POST"
}


resource "aws_apigatewayv2_route" "get" {

  api_id = aws_apigatewayv2_api.lambda.id
  route_key = "GET /users/{userId}/inventories/{inventoryId}"
  target = "integrations/${aws_apigatewayv2_integration.get_inventory.id}"
}


resource "aws_apigatewayv2_route" "insert" {

  api_id = aws_apigatewayv2_api.lambda.id
  route_key = "POST /users/{userId}/inventories"
  target = "integrations/${aws_apigatewayv2_integration.insert_inventory.id}"
}


resource "aws_apigatewayv2_route" "list" {

  api_id = aws_apigatewayv2_api.lambda.id
  route_key = "GET /users/{userId}/inventories"
  target = "integrations/${aws_apigatewayv2_integration.list_inventory.id}"
}

resource "aws_apigatewayv2_route" "update" {

  api_id = aws_apigatewayv2_api.lambda.id
  route_key = "PATCH /users/{userId}/inventories/{inventoryId}"
  target = "integrations/${aws_apigatewayv2_integration.update_inventory.id}"
}


resource "aws_apigatewayv2_route" "delete" {

  api_id = aws_apigatewayv2_api.lambda.id
  route_key = "DELETE /users/{userId}/inventories/{inventoryId+}"
  target = "integrations/${aws_apigatewayv2_integration.delete_inventory.id}"
}

resource "aws_cloudwatch_log_group" "api_gw" {
  name = "/aws/api_gw/${aws_apigatewayv2_api.lambda.name}"
  retention_in_days = 30
}


resource "aws_lambda_permission" "get_permission" {
  statement_id = "AllowExecutionFromAPIGateway"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.get_inventory.function_name
  principal = "apigateway.amazonaws.com"

  source_arn = "${aws_apigatewayv2_api.lambda.execution_arn}/*/*"
}

resource "aws_lambda_permission" "insert_permission" {
  statement_id = "AllowExecutionFromAPIGateway"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.insert_inventory.function_name
  principal = "apigateway.amazonaws.com"

  source_arn = "${aws_apigatewayv2_api.lambda.execution_arn}/*/*"
}

resource "aws_lambda_permission" "update_permission" {
  statement_id = "AllowExecutionFromAPIGateway"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.update_inventory.function_name
  principal = "apigateway.amazonaws.com"

  source_arn = "${aws_apigatewayv2_api.lambda.execution_arn}/*/*"
}

resource "aws_lambda_permission" "list_permission" {
  statement_id = "AllowExecutionFromAPIGateway"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.list_inventory.function_name
  principal = "apigateway.amazonaws.com"

  source_arn = "${aws_apigatewayv2_api.lambda.execution_arn}/*/*"
}

resource "aws_lambda_permission" "delete_permission" {
  statement_id = "AllowExecutionFromAPIGateway"
  action = "lambda:InvokeFunction"
  function_name = aws_lambda_function.delete_inventory.function_name
  principal = "apigateway.amazonaws.com"

  source_arn = "${aws_apigatewayv2_api.lambda.execution_arn}/*/*"
}







