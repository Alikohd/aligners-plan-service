package ru.etu.controlservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.AlignmentPayload;
import ru.etu.controlservice.dto.task.ResultPlanningPayload;
import ru.etu.controlservice.dto.task.SegmentationCtPayload;
import ru.etu.controlservice.dto.task.SegmentationJawPayload;
import ru.etu.controlservice.dto.task.TreatmentPlanningPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.service.processor.TaskProcessor;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskQueueHandler {
    private final List<TaskProcessor> taskProcessors;
    private final NodeRepository nodeRepository;
    private final ObjectMapper objectMapper;
    private Map<NodeType, TaskProcessor> processorMap;

    @PostConstruct
    public void initProcessorMap() {
        processorMap = taskProcessors.stream()
                .collect(Collectors.toMap(TaskProcessor::getSupportedType, Function.identity()));
        log.info("Initialized {} task processors", processorMap.size());
    }

    @ServiceActivator(inputChannel = "tasksQueue", poller = @Poller(value = "tasksPoller"), adviceChain = "retryAdvice")
    public void handleTask(Message<?> message) {
        try {
            String payload = (String) message.getPayload();
            Map<String, Object> headers = message.getHeaders();
            NodeType nodeType = NodeType.valueOf((String) headers.get("nodeType"));
            UUID nodeId = UUID.fromString((String) Objects.requireNonNull(headers.get("nodeId")));

            Node node = nodeRepository.findById(nodeId)
                    .orElseThrow(() -> new MessagingException("Node not found: " + nodeId));

            TaskProcessor processor = processorMap.get(nodeType);
            if (processor == null) {
                throw new MessagingException("No processor found for NodeType: " + nodeType);
            }

            // Десериализация payload
            Object taskPayload = deserializePayload(payload, nodeType);
            processor.process(taskPayload, node); // Передаём десериализованный объект
            log.info("Processed task for NodeType: {}, Node: {}", nodeType, nodeId);
        } catch (Exception e) {
            log.error("Failed to process task: {}", e.getMessage(), e);
            throw new MessagingException("Task processing failed", e); // Сообщение остаётся в очереди
        }
    }

    private Object deserializePayload(String payload, NodeType nodeType) throws Exception {
        return switch (nodeType) {
            case SEGMENTATION_CT -> objectMapper.readValue(payload, SegmentationCtPayload.class);
            case SEGMENTATION_JAW -> objectMapper.readValue(payload, SegmentationJawPayload.class);
            case SEGMENTATION_ALIGNMENT -> objectMapper.readValue(payload, AlignmentPayload.class);
            case RESULT_PLANNING -> objectMapper.readValue(payload, ResultPlanningPayload.class);
            case TREATMENT_PLANNING -> objectMapper.readValue(payload, TreatmentPlanningPayload.class);
            case EMPTY_NODE -> throw new EntityNotFoundException("Empty node cannot be processed");
        };
    }
}