package ru.etu.controlservice.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.etu.controlservice.dto.TaskCreatedEvent;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.repository.TaskRepository;
import ru.etu.controlservice.service.processor.TaskProcessor;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {
    private final List<TaskProcessor> taskProcessors;
    private final TaskRepository taskRepository;
    private Map<NodeType, TaskProcessor> processorMap;
    private final TaskServiceAsyncHelper asyncHelper;
    private final ApplicationEventPublisher eventPublisher;

    @PostConstruct
    public void init() {
        processorMap = taskProcessors.stream()
                .collect(Collectors.toMap(TaskProcessor::getSupportedType, Function.identity()));
        log.info("Initialized {} task processors", processorMap.size());
    }

    public void addTask(String payload, NodeType type, Node node) {
        log.info("Thread in addTask: {}", Thread.currentThread().getName());
        Task task = new Task(payload, type, node);
        Task savedTask = taskRepository.save(task);
        eventPublisher.publishEvent(new TaskCreatedEvent(savedTask));
    }

    public void processTask(Task task) {
        TaskProcessor processor = processorMap.get(task.getType());
        asyncHelper.processTaskAsync(task, processor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCreated(TaskCreatedEvent event) {
        Task task = event.task();
        processTask(task);
    }
}
