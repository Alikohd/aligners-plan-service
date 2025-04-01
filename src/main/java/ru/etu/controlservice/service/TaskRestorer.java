package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.entity.TaskStatus;
import ru.etu.controlservice.repository.TaskRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRestorer implements ApplicationListener<ApplicationReadyEvent> {
    private final TaskService taskService;
    private final TaskRepository taskRepository;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        recoverUnfinishedTasks();
    }

    public void recoverUnfinishedTasks() {
        List<Task> inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS);
        if (!inProgressTasks.isEmpty()) {
            log.info("Recovering {} unfinished tasks", inProgressTasks.size());
            inProgressTasks.forEach(taskService::processTask);
        } else {
            log.info("No unfinished tasks found to recover");
        }
    }
}
