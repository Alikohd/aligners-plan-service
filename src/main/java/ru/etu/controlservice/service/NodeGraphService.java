package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.FlatNodeDto;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.util.NodeContentUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NodeGraphService {
    private final TreatmentCaseService treatmentCaseService;

    public List<FlatNodeDto> getFlatGraph(UUID patientId, UUID caseId) {
        var treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);

        Node root = treatmentCase.getRoot();
        if (root == null) {
            return Collections.emptyList();
        }

        List<FlatNodeDto> result = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (!visited.add(node.getId())) {
                continue;
            }

            List<Node> children = node.getNextNodes();
            List<UUID> childIds = new ArrayList<>();
            if (children != null) {
                for (Node child : children) {
                    childIds.add(child.getId());
                    queue.add(child);
                }
            }

            result.add(new FlatNodeDto(node.getId(), NodeContentUtils.getNodeType(node), childIds));
        }

        return result;
    }
}
