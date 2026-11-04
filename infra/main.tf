terraform {
  required_version = ">= 1.5.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

locals {
  image = var.image != "" ? var.image : "${var.region}-docker.pkg.dev/${var.project_id}/${var.artifact_repo}/${var.service_name}:latest"
}

# --- APIs the service depends on -------------------------------------------
resource "google_project_service" "apis" {
  for_each = toset([
    "run.googleapis.com",
    "artifactregistry.googleapis.com",
    "secretmanager.googleapis.com",
    "cloudbuild.googleapis.com",
  ])

  service            = each.value
  disable_on_destroy = false
}

# --- Artifact Registry: where Cloud Build pushes images --------------------
resource "google_artifact_registry_repository" "apps" {
  location      = var.region
  repository_id = var.artifact_repo
  format        = "DOCKER"
  description   = "Container images deployed to Cloud Run"

  depends_on = [google_project_service.apis]
}

# --- Runtime identity: the service runs as its own service account ---------
resource "google_service_account" "run" {
  account_id   = "${var.service_name}-run"
  display_name = "Cloud Run runtime for ${var.service_name}"
}

# --- Secret Manager: API token the app reads as the API_TOKEN env var ------
resource "google_secret_manager_secret" "api_token" {
  secret_id = "api-token"

  replication {
    auto {}
  }

  depends_on = [google_project_service.apis]
}

# Placeholder value so the wiring is complete out of the box; rotate with
# `gcloud secrets versions add api-token --data-file=-` before real use.
resource "google_secret_manager_secret_version" "api_token" {
  secret      = google_secret_manager_secret.api_token.id
  secret_data = "change-me-before-real-use"
}

resource "google_secret_manager_secret_iam_member" "run_access" {
  secret_id = google_secret_manager_secret.api_token.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.run.email}"
}

# --- Cloud Run service ------------------------------------------------------
resource "google_cloud_run_v2_service" "app" {
  name     = var.service_name
  location = var.region
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    service_account = google_service_account.run.email

    scaling {
      min_instance_count = 0
      max_instance_count = 4
    }

    containers {
      image = local.image

      ports {
        container_port = 8080
      }

      # Secret Manager wiring: the secret becomes a plain env var in the
      # container, so the app never holds GCP credentials itself.
      env {
        name = "API_TOKEN"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.api_token.secret_id
            version = "latest"
          }
        }
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      startup_probe {
        http_get {
          path = "/actuator/health"
        }
      }

      liveness_probe {
        http_get {
          path = "/actuator/health"
        }
      }
    }
  }

  depends_on = [
    google_project_service.apis,
    google_secret_manager_secret_iam_member.run_access,
  ]
}

# Public access toggle (set allow_unauthenticated = false to keep it private).
resource "google_cloud_run_v2_service_iam_member" "public" {
  count = var.allow_unauthenticated ? 1 : 0

  name     = google_cloud_run_v2_service.app.name
  location = var.region
  role     = "roles/run.invoker"
  member   = "allUsers"
}
