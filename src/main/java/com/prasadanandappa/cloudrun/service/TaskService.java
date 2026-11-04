package com.prasadanandappa.cloudrun.service;

import com.prasadanandappa.cloudrun.model.Task;
import com.prasadanandappa.cloudrun.model.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory task store.
 *
 * <p>Thread-safe by construction ({@link ConcurrentHashMap} + {@link AtomicLong})
 * because Cloud Run serves requests concurrently on a single instance.
 * The clock is injected so tests can assert timestamp behaviour deterministically.
 */
@Service
public class TaskService {

    private final ConcurrentHashMap<Long, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(0);
    private final Clock clock;

    public TaskService() {
        this(Clock.systemUTC());
    }

    TaskService(Clock clock) {
        this.clock = clock;
    }

    public Task create(String title) {
        long id = nextId.incrementAndGet();
        Instant now = clock.instant();
        Task task = new Task(id, title.trim(), TaskStatus.TODO, now, now);
        tasks.put(id, task);
        return task;
    }

    public List<Task> list() {
        return tasks.values().stream()
                .sorted(Comparator.comparingLong(Task::id))
                .toList();
    }

    public Task get(long id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    public Task updateStatus(long id, TaskStatus status) {
        Task updated = get(id).withStatus(status, clock.instant());
        tasks.put(id, updated);
        return updated;
    }

    public void delete(long id) {
        if (tasks.remove(id) == null) {
            throw new TaskNotFoundException(id);
        }
    }
}
