package com.prasadanandappa.cloudrun.model;

import java.time.Instant;

/** Immutable task entity as stored and returned by the API. */
public record Task(
        long id,
        String title,
        TaskStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public Task withStatus(TaskStatus newStatus, Instant when) {
        return new Task(id, title, newStatus, createdAt, when);
    }
}
