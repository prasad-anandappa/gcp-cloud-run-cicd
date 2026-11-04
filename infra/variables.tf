variable "project_id" {
  description = "GCP project to deploy into. Placeholder by design — set with -var or a tfvars file."
  type        = string
  default     = "your-gcp-project"
}

variable "region" {
  description = "Region for Cloud Run and Artifact Registry."
  type        = string
  default     = "us-central1"
}

variable "service_name" {
  description = "Cloud Run service name."
  type        = string
  default     = "task-tracker"
}

variable "artifact_repo" {
  description = "Artifact Registry Docker repository name."
  type        = string
  default     = "apps"
}

variable "image" {
  description = "Container image to run. Defaults to the Artifact Registry path Cloud Build pushes to."
  type        = string
  default     = ""
}

variable "allow_unauthenticated" {
  description = "Whether to grant roles/run.invoker to allUsers. Turn off for private services."
  type        = bool
  default     = true
}
