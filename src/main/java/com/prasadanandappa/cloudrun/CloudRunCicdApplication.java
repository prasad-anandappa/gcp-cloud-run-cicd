package com.prasadanandappa.cloudrun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Cloud Run task-tracker service.
 *
 * <p>The service is intentionally stateless (in-memory store) so the demo stays
 * focused on the platform pattern: container build, Cloud Build pipeline,
 * Terraform-provisioned Cloud Run + Artifact Registry + Secret Manager.
 * In a real deployment the store would be swapped for Cloud SQL / Firestore
 * behind the same {@code TaskService} interface.
 */
@SpringBootApplication
public class CloudRunCicdApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudRunCicdApplication.class, args);
    }
}
