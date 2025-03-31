package ru.etu.controlservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.dto.task.ResultPlanningPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.NodesRequiredForAlignmentNotFoundException;
import ru.etu.controlservice.exceptions.StepAlreadyExistException;
import ru.etu.controlservice.mapper.NodeMapper;
import ru.etu.controlservice.util.NodeContentUtils;

import java.util.List;
import java.util.Map;

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
    public NodeDto startResultPlanning(Long patientId, Long caseId) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        boolean resultPlanningAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getResultPlanning() != null);
        if (resultPlanningAlreadyExists) {
            throw new StepAlreadyExistException("There is already ResultPlanning in latest branch. Use correction api to change it");
        }

        Node resultPlanningNode = nodeService.addStep(tCase);

        Map<NodeType, Node> requiredNodes = nodeContentUtils.getPrevNodes(resultPlanningNode, NODES_REQUIRED_FOR_RESULT_PLANNING);
        if (requiredNodes.size() != NODES_REQUIRED_FOR_RESULT_PLANNING.size()) {
            throw new NodesRequiredForAlignmentNotFoundException("Nodes required for ResultPlanning were not found!");
        }

        ResultPlanningPayload payload = new ResultPlanningPayload(requiredNodes.get(NodeType.SEGMENTATION_ALIGNMENT).getId());

        try {
            taskService.addTask(objectMapper.writeValueAsString(payload), NodeType.RESULT_PLANNING, resultPlanningNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        return nodeMapper.toDto(resultPlanningNode);
    }
}
