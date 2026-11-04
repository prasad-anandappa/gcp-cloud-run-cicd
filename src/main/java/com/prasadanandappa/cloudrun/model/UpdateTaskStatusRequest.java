package com.prasadanandappa.cloudrun.model;

import jakarta.validation.constraints.NotNull;

/** Request body for {@code PATCH /api/tasks/{id}/status}. */
public record UpdateTaskStatusRequest(
        @NotNull(message = "status is required")
        TaskStatus status) {
}
