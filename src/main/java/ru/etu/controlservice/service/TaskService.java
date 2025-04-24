package ru.etu.controlservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TaskService {
    private final MessageChannel taskQueueChannel;

    public TaskService(@Qualifier("tasksQueue") MessageChannel taskQueueChannel) {
        this.taskQueueChannel = taskQueueChannel;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void addTask(String payload, NodeType type, Node node) {
        log.info("Adding task for NodeType: {}, Node: {}", type, node.getId());
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("nodeType", type.name());
            headers.put("nodeId", node.getId().toString());

            taskQueueChannel.send(new GenericMessage<>(payload, headers));
            log.info("Task sent to queue for NodeType: {}", type);
        } catch (Exception e) {
            log.error("Failed to send task to queue for NodeType: {}", type, e);
            throw new RuntimeException("Failed to send task to queue", e);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void addTask(String payload, NodeType type) {
        log.info("Adding task for NodeType: {}", type);
        try {
            Map<String, Object> headers = new HashMap<>();
            headers.put("nodeType", type.name());
            taskQueueChannel.send(new GenericMessage<>(payload, headers));
            log.info("Task without NodeId sent to queue for NodeType: {}", type);
        } catch (Exception e) {
            log.error("Failed to send task to queue for NodeType: {}", type, e);
            throw new RuntimeException("Failed to send task to queue", e);
        }
    }

}
