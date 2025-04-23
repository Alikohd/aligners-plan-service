package ru.etu.controlservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.dto.task.TreatmentPlanningPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.NodesRequiredForAlignmentNotFoundException;
import ru.etu.controlservice.exceptions.StepAlreadyExistException;
import ru.etu.controlservice.mapper.NodeMapper;
import ru.etu.controlservice.util.NodeContentUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanningService {
    private final TreatmentCaseService caseService;
    private final NodeService nodeService;
    private final NodeContentUtils nodeContentUtils;
    private final NodeMapper nodeMapper;
    private final TaskService taskService;
    private final ObjectMapper objectMapper;
    private final List<NodeType> NODES_REQUIRED_FOR_TREATMENT_PLANNING = List.of(NodeType.RESULT_PLANNING);

    @Transactional
    public NodeDto startTreatmentPlanning(UUID patientId, UUID caseId) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        boolean treatmentPlanningAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getTreatmentPlanning() != null);
        if (treatmentPlanningAlreadyExists) {
            throw new StepAlreadyExistException("There is already TreatmentPlanning. Use correction api to change it");
        }

        Node treatmentPlanningNode = nodeService.addStepToEnd(tCase);

        Map<NodeType, Node> requiredNodes = nodeContentUtils.getPrevNodes(treatmentPlanningNode, NODES_REQUIRED_FOR_TREATMENT_PLANNING);
        if (requiredNodes.size() != NODES_REQUIRED_FOR_TREATMENT_PLANNING.size()) {
            throw new NodesRequiredForAlignmentNotFoundException("Nodes required for TreatmentPlanning were not found!");
        }

        TreatmentPlanningPayload payload = new TreatmentPlanningPayload(requiredNodes.get(NodeType.RESULT_PLANNING).getId());

        try {
            taskService.addTask(objectMapper.writeValueAsString(payload), NodeType.TREATMENT_PLANNING, treatmentPlanningNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        return nodeMapper.toDto(treatmentPlanningNode);
    }
}
