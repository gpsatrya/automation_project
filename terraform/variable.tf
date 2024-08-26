## For General
variable "google_application_credentials" {
  description = "Path to the Google Cloud service account key file"
  type        = string
}

variable "id_project" {
  description = "Project ID"
  type        = string
  default     = ""
}

variable "region" {
  description = "GCP region"
  type        = string
  default     = ""
}

variable "zone" {
  description = "For GCP workload region"
  type        = string
  default     = ""
}

## For Compute Engine
variable "instance_name" {
  description = "For google compute instance name"
  type        = string
  default     = ""
}

variable "instance_type" {
  description = "Compute Instance machine type"
  type        = string
  default     = ""
}

variable "instance_os" {
  description = "Google compute engine OS"
  type        = string
  default     = ""
}

variable "disk_size" {
  description = "disk for compute engine"
  type        = string
  default     = ""
}

variable "instance_count" {
  description = "Number of instance"
  type        = number
  default     = 0
  
}

## For VPC Network
/*
variable "vpc_name" {
  description = "For VPC network name"
  type        = string
}

## For Subnet 
variable "subnet_name" {
  description = "For Subnet name"
  type        = string
}
*/