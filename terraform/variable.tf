## For General
variable "google_application_credentials" {
  description = "Path to the Google Cloud service account key file"
  type        = string
}

variable "id_project" {
  description = "Project ID"
  type        = string
}

variable "region" {
  description = "GCP region"
  type        = string
}

variable "zone" {
  description = "For GCP workload region"
  type        = string
}

## For Compute Engine
variable "instance_name" {
  description = "For google compute instance name"
  type        = string
}

variable "instance_type" {
  description = "Compute Instance machine type"
  type        = string
}

variable "instance_os" {
  description = "Google compute engine OS"
  type        = string
}

variable "disk_size" {
  description = "disk for compute engine"
  type        = string
}

variable "instance_count" {
  description = "Number of instance"
  type        = number
  
}

## For VPC Network
variable "vpc_name" {
  description = "For VPC network name"
  type        = string
}

## For Subnet 
variable "subnet_name" {
  description = "For Subnet name"
  type        = string
}