variable "aws_region" {
  description = "AWS region for all resources."

  type    = string
  default = "us-east-1"
}

// variable for account
variable "aws_account" {
  description = "AWS account"
  type = string
}

// variable for ddb
variable "env" {
  description = "environment flag"
  type = string
  default = "dev"
}