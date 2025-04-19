package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.entity.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByStatusAndType(TaskStatus status, NodeType type);
}