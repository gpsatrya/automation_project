data "google_compute_network" "network_vpc" {
  name   = var.vpc_name
}

data "google_compute_subnetwork" "subnet" {
  name   = var.subnet_name
  region = var.region 
}

resource "google_compute_instance" "instance" {
  count         = var.instance_count
  name          = "${var.instance_name}-${count.index}"
  machine_type  = var.instance_type
  zone          = var.zone

  boot_disk {
    initialize_params {
      image = var.instance_os
      size  = var.disk_size
      type  = "pd-balanced"
    }
  }

  network_interface {
    network = "default"
    access_config {
      Include this section to give the VM a public IP address
    }
    //network     = data.google_compute_network.network_vpc.id
    //subnetwork  = data.google_compute_subnetwork.subnet.id

    //access_config {
    //    network_tier = "STANDARD"
    //}
  }

  // Metadata startup script for automatic shutdown
  metadata_startup_script = <<-EOT
    #!/bin/bash
    echo "Startup script running"
    # Shutdown after 1 hour to save costs
    shutdown -h +60
  EOT
}
