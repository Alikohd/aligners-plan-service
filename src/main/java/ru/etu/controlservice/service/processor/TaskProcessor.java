package ru.etu.controlservice.service.processor;

import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.Task;

public interface TaskProcessor {
    void process(Task task);
    NodeType getSupportedType();
}