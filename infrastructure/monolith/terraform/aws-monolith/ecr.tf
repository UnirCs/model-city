locals {
  # Images managed as a group (ECR). Monolithic topology: one backend artifact + frontend.
  services = [
    "monolith",
    "frontend",
  ]
}

resource "aws_ecr_repository" "this" {
  for_each             = toset(local.services)
  name                 = "${var.project}/${each.value}"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
  tags                 = { Name = "${local.name}-${each.value}-repo" }

  image_scanning_configuration {
    scan_on_push = true
  }
}

# Keep only the last 10 images per repo to stay within Academy storage quotas.
resource "aws_ecr_lifecycle_policy" "this" {
  for_each   = aws_ecr_repository.this
  repository = each.value.name
  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "Keep last 10 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 10
      }
      action = { type = "expire" }
    }]
  })
}
