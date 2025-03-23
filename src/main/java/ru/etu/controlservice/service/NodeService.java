package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeNextRelation;
import ru.etu.controlservice.entity.NodePrevRelation;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.repository.TreatmentCaseRepository;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class NodeService {

    private final NodeRepository nodeRepository;
    private final TreatmentCaseRepository treatmentCaseRepository;

    @Transactional
    public Node createStep(TreatmentCase treatmentCase) {
        Node rootNode = treatmentCase.getRoot();

        if (rootNode == null) {
            return createInitialNode(treatmentCase);
        }

        Node lastNode = findLastNode(rootNode);
        return appendNewNode(lastNode);
    }

    private Node createInitialNode(TreatmentCase treatmentCase) {
        Node newNode = Node.builder()
                .treatmentBranchId(1L)
                .build();

//        Node savedNode = nodeRepository.save(newNode);
        treatmentCase.setRoot(newNode);
        treatmentCaseRepository.save(treatmentCase);

        return newNode;
    }

    private Node findLastNode(Node startNode) {
        Node currentNode = startNode;

        while (!currentNode.getNextNodes().isEmpty()) {
            currentNode = currentNode.getNextNodes().stream()
                    .max(Comparator.comparing(relation -> relation.getNextNode().getTreatmentBranchId()))
                    .map(NodeNextRelation::getNextNode)
                    .orElseThrow(() -> new IllegalStateException("Unexpected empty optional in node chain"));
        }

        return currentNode;
    }

    private Node appendNewNode(Node previousNode) {
        Node newNode = Node.builder()
                .treatmentBranchId(previousNode.getTreatmentBranchId())
                .build();

        newNode = nodeRepository.save(newNode);
        createBidirectionalRelation(previousNode, newNode);

        nodeRepository.save(previousNode);
        return newNode;
    }

    private void createBidirectionalRelation(Node previousNode, Node newNode) {
        NodeNextRelation nextRelation = NodeNextRelation.builder()
                .node(previousNode)
                .nextNode(newNode)
                .build();

        NodePrevRelation prevRelation = NodePrevRelation.builder()
                .node(newNode)
                .prevNode(previousNode)
                .build();

        previousNode.getNextNodes().add(nextRelation);
        newNode.getPrevNodes().add(prevRelation);
    }
}
