package ru.etu.controlservice.service.processor;

import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;

public interface TaskProcessor {
    void process(Object payload, Node node);

    NodeType getSupportedType();
}