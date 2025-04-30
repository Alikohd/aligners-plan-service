package ru.etu.controlservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.dto.MetaNodeDto;
import ru.etu.controlservice.dto.task.ResultPlanningPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.mapper.NodeMapper;
import ru.etu.controlservice.util.NodeContentUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResultPlanningService {
    private final TreatmentCaseService caseService;
    private final NodeService nodeService;
    private final NodeContentUtils nodeContentUtils;
    private final NodeMapper nodeMapper;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final List<NodeType> NODES_REQUIRED_FOR_RESULT_PLANNING = List.of(NodeType.SEGMENTATION_ALIGNMENT);

    @Transactional
    public MetaNodeDto startResultPlanning(UUID patientId, UUID caseId, UUID nodeId) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        Node resultPlanningNode;
        if (nodeId != null) {
            Node currentNode = nodeService.getNode(nodeId);
            resultPlanningNode = nodeService.addStepTo(currentNode);
        } else {
            resultPlanningNode = nodeService.addStepToEnd(tCase);
        }
        return pendResultTask(resultPlanningNode);
    }

    public MetaNodeDto adjustResultInline(UUID patientId, UUID caseId, UUID nodeId, List<JsonNode> desiredTeethMatrices) {
        caseService.getCaseById(patientId, caseId);
        Node currentResultNode = nodeService.getNode(nodeId);
        currentResultNode.getResultPlanning().setDesiredTeethMatrices(desiredTeethMatrices);
        Node updatedNode = nodeService.updateNode(currentResultNode);
        return nodeMapper.toDto(updatedNode);
    }

    @Transactional
    public MetaNodeDto adjustResult(UUID patientId, UUID caseId, UUID nodeId) {
        caseService.getCaseById(patientId, caseId);
        Node currentResultNode = nodeService.getNode(nodeId);
        Node newNode = nodeService.addStepTo(currentResultNode.getPrevNode());
        return pendResultTask(newNode);
    }

    private MetaNodeDto pendResultTask(Node newNode) {
        Map<NodeType, Node> requiredNodes = nodeContentUtils.getRequiredPrevNodes(newNode, NODES_REQUIRED_FOR_RESULT_PLANNING);
        ResultPlanningPayload payload = new ResultPlanningPayload(requiredNodes.get(NodeType.SEGMENTATION_ALIGNMENT).getId());
        try {
            taskService.addTask(objectMapper.writeValueAsString(payload), NodeType.RESULT_PLANNING, newNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        return nodeMapper.toDto(newNode);
    }
}
