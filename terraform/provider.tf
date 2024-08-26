terraform {
  backend "local" {
    path = "../../terraform_state/terraform.tfstate"
  }
  required_providers {
    google = {
      source    = "hashicorp/google"
      version   = "5.41.0"
    }
  }
}

provider "google" {
  credentials = file(var.google_application_credentials)
  project     = "var.id_project"
  region      = "var.region"
}
