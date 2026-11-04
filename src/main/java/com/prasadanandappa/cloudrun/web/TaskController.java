package com.prasadanandappa.cloudrun.web;

import com.prasadanandappa.cloudrun.model.CreateTaskRequest;
import com.prasadanandappa.cloudrun.model.Task;
import com.prasadanandappa.cloudrun.model.UpdateTaskStatusRequest;
import com.prasadanandappa.cloudrun.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** Service banner — handy as a smoke check right after a Cloud Run deploy. */
    @GetMapping("/")
    public Map<String, String> index() {
        return Map.of(
                "service", "gcp-cloud-run-cicd",
                "status", "ok",
                "docs", "See README.md for the API and deploy walkthrough");
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request.title());
    }

    @GetMapping("/tasks")
    public List<Task> list() {
        return taskService.list();
    }

    @GetMapping("/tasks/{id}")
    public Task get(@PathVariable long id) {
        return taskService.get(id);
    }

    @PatchMapping("/tasks/{id}/status")
    public Task updateStatus(@PathVariable long id,
                             @Valid @RequestBody UpdateTaskStatusRequest request) {
        return taskService.updateStatus(id, request.status());
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskService.delete(id);
    }
}
