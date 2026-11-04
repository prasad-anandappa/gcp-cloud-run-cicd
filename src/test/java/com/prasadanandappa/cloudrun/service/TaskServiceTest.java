package com.prasadanandappa.cloudrun.service;

import com.prasadanandappa.cloudrun.model.Task;
import com.prasadanandappa.cloudrun.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskServiceTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService(Clock.fixed(T0, ZoneOffset.UTC));
    }

    @Test
    void createAssignsSequentialIdsAndTodoStatus() {
        Task first = service.create("Write Terraform");
        Task second = service.create("Wire Cloud Build");

        assertThat(first.id()).isEqualTo(1L);
        assertThat(second.id()).isEqualTo(2L);
        assertThat(first.status()).isEqualTo(TaskStatus.TODO);
        assertThat(first.createdAt()).isEqualTo(T0);
        assertThat(first.updatedAt()).isEqualTo(T0);
    }

    @Test
    void createTrimsTitle() {
        assertThat(service.create("  ship it  ").title()).isEqualTo("ship it");
    }

    @Test
    void listReturnsTasksInIdOrder() {
        service.create("b");
        service.create("a");
        service.create("c");

        List<Task> tasks = service.list();
        assertThat(tasks).extracting(Task::id).containsExactly(1L, 2L, 3L);
    }

    @Test
    void getMissingTaskThrows() {
        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateStatusChangesStatusAndKeepsCreatedAt() {
        Task created = service.create("Deploy to Cloud Run");

        Task updated = service.updateStatus(created.id(), TaskStatus.IN_PROGRESS);

        assertThat(updated.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updated.createdAt()).isEqualTo(created.createdAt());
        assertThat(service.get(created.id()).status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void updateStatusOnMissingTaskThrows() {
        assertThatThrownBy(() -> service.updateStatus(42L, TaskStatus.DONE))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteRemovesTaskAndSecondDeleteThrows() {
        Task created = service.create("Temporary");

        service.delete(created.id());

        assertThat(service.list()).isEmpty();
        assertThatThrownBy(() -> service.delete(created.id()))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
