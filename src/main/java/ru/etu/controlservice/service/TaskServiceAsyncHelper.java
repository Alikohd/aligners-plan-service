package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.entity.TaskStatus;
import ru.etu.controlservice.repository.TaskRepository;
import ru.etu.controlservice.service.processor.TaskProcessor;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskServiceAsyncHelper {
    private final TaskRepository taskRepository;

    @Transactional(propagation = Propagation.NEVER)
    @Async("segmentationTaskExecutor")
    public void processTaskAsync(Task task, TaskProcessor processor) {
        log.info("Thread in processTaskAsync: {}", Thread.currentThread().getName());
        setTaskInProgress(task.getId());
        if (processor == null) {
            throw new IllegalStateException("No processor found for task type: " + task.getType());
        }

        try {
            processor.process(task);
            setTaskCompleted(task.getId());
        } catch (Exception e) {
            setTaskFailed(task.getId(), e);
        }
    }

    public void setTaskInProgress(Long taskId) {
        extracted(TaskStatus.IN_PROGRESS, taskId, null);
    }

    public void setTaskCompleted(Long taskId) {
        extracted(TaskStatus.COMPLETED, taskId, null);
    }

    public void setTaskFailed(Long taskId, Exception e) {
        extracted(TaskStatus.FAILED, taskId, e);
    }

    private void extracted(TaskStatus taskStatus, Long taskId, Exception e) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("No task found with id: " + taskId));
        if (e != null) {
            log.error("Task {} failed", taskId, e);
        }
        task.setStatus(taskStatus);
        task.setUpdatedAt(Instant.now());
        taskRepository.save(task);
    }
}
