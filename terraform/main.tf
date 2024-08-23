provider "google" {
  credentials = file("/home/gsatrya/secrets/panji-sandbox-49d7ea9ef84b.json")
  project     = "panji-sandbox"
  region      = "us-central1"
}

resource "google_compute_instance" "default" {
  name         = "automate-instance-1"
  machine_type = "e2-small"
  zone         = "us-central1-a"

  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-11"
    }
  }

  network_interface {
    network = "default"
    access_config {
      // Include this section to give the VM a public IP address
    }
  }

  // Metadata startup script for automatic shutdown
  metadata_startup_script = <<-EOT
    #!/bin/bash
    echo "Startup script running"
    # Shutdown after 1 hour to save costs
    shutdown -h +60
  EOT
}
