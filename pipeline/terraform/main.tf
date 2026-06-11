terraform {
  required_version = ">= 1.7.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 3.100.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = ">= 2.29.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = ">= 2.13.0"
    }
  }
}

# Safety-first assessment module.
# No resources are created by default. This file validates provider configuration
# and documents the intended Azure/AKS path without consuming free-trial credits.
provider "azurerm" {
  features {}
}

variable "enable_aks_demo" {
  type        = bool
  default     = false
  description = "Set true only if you intentionally add AKS resources for a paid/cloud demo."
}

output "assessment_note" {
  value = "Terraform is configured for Azure validation. Add AKS resources only when you are ready to incur cloud usage."
}
