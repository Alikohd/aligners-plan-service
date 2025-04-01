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
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NodeService {

    private final NodeRepository nodeRepository;
    private final TreatmentCaseRepository treatmentCaseRepository;

    @Transactional
    public Node addStep(TreatmentCase treatmentCase) {
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

        treatmentCase.setRoot(newNode);
        treatmentCaseRepository.save(treatmentCase);

        return newNode;
    }

    private Node findLastNode(Node startNode) {
        return traverseNodes(startNode)
                .reduce((first, second) -> second) // Берем последний элемент потока
                .orElse(startNode); // Если поток пустой, возвращаем начальный узел
    }

//    todo: handle n+1 trouble?
    public Stream<Node> traverseNodes(Node startNode) {
        return Stream.iterate(
                startNode,
                Objects::nonNull,
                node -> node.getNextNodes().stream()
                        .max(Comparator.comparing(relation -> relation.getNextNode().getTreatmentBranchId()))
                        .map(NodeNextRelation::getNextNode)
                        .orElse(null)
        );
    }

    @Transactional
    protected Node appendNewNode(Node previousNode) {
        Node newNode = Node.builder()
                .treatmentBranchId(previousNode.getTreatmentBranchId())
                .build();

        createBidirectionalRelation(previousNode, newNode);
        newNode = nodeRepository.save(newNode);
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
