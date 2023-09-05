output "lambda_bucket_name" {
  description = "Name of the S3 bucket used to store function code."

  value = aws_s3_bucket.lambda_bucket.id
}

output "ddb_table" {
  description = "Name of the Inventory Table Name"
  value = aws_dynamodb_table.inventoryTable.name
}

output "get_inventory_function_name" {
  description = "Get Inventory Function Name."

  value = aws_lambda_function.get_inventory.function_name
}

output "insert_inventory_function_name" {
  description = "Insert Inventory Function Name."

  value = aws_lambda_function.insert_inventory.function_name
}
output "update_inventory_function_name" {
  description = "Update Inventory Function Name."

  value = aws_lambda_function.update_inventory.function_name
}
output "list_inventory_function_name" {
  description = "List Inventory Function Name."

  value = aws_lambda_function.list_inventory.function_name
}

output "delete_inventory_function_name" {
  description = "Delete Inventory Function Name."

  value = aws_lambda_function.delete_inventory.function_name
}

output "base_url" {
  description = "Base URL for API Gateway stage."

  value = aws_apigatewayv2_stage.lambda.invoke_url
}