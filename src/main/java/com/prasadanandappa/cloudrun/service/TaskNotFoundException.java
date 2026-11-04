package com.prasadanandappa.cloudrun.service;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(long id) {
        super("Task not found: " + id);
    }
}
