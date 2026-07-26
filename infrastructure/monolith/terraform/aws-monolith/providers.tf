terraform {
  required_version = ">= 1.6.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.70"
    }
  }
  # Local state by default. Switch to S3 backend later if multi-developer.
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project   = "model-city"
      ManagedBy = "terraform"
      Env       = var.env
    }
  }
}

# AWS Academy Learner Lab ships with a pre-created role called "LabRole" that
# we are forced to reuse (we cannot create new IAM roles). All ECS tasks and
# RDS enhanced monitoring will use this role.
data "aws_iam_role" "lab" {
  name = var.lab_role_name
}

data "aws_availability_zones" "available" {
  state = "available"
}

