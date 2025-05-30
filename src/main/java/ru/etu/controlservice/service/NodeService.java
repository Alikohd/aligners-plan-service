package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.NodeNotFoundException;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.repository.TreatmentCaseRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NodeService {

    private final NodeRepository nodeRepository;
    private final TreatmentCaseRepository treatmentCaseRepository;

    @Transactional
    public Node addStepToEnd(TreatmentCase treatmentCase) {
        Node rootNode = treatmentCase.getRoot();

        if (rootNode == null) {
            return createInitialNode(treatmentCase);
        }

        Node lastNode = findLastNode(rootNode);
        return addStepTo(lastNode);
    }

    private Node createInitialNode(TreatmentCase treatmentCase) {
        Node newNode = new Node();
        treatmentCase.setRoot(newNode);
        treatmentCaseRepository.save(treatmentCase);

        return newNode;
    }

    public Node findLastNode(Node startNode) {
        return traverseNodes(startNode)
                .reduce((first, second) -> second) // Берем последний элемент потока
                .orElse(startNode); // Если поток пустой, возвращаем начальный узел
    }

    public Stream<Node> traverseNodes(Node startNode) {
        return Stream.iterate(
                startNode,
                Objects::nonNull,
                node -> {
                    System.out.println("Current node ID: " + node.getId());

                    List<Node> nextNodes = node.getNextNodes();
                    if (nextNodes == null || nextNodes.isEmpty()) {
                        System.out.println("No next nodes for node ID: " + node.getId());
                        return null;
                    }

                    Node next = nextNodes.stream()
                            .max(Comparator.comparing(Node::getCreatedAt))
                            .orElse(null);

                    System.out.println("Next node ID: " + next.getId());
                    return next;
                }
        );
    }

    @Transactional
    public Node addStepTo(Node previousNode) {
        Node newNode = new Node();

        createBidirectionalRelation(previousNode, newNode);
        newNode = nodeRepository.save(newNode);
        nodeRepository.save(previousNode);

        return newNode;
    }

    private void createBidirectionalRelation(Node previousNode, Node newNode) {
        previousNode.getNextNodes().add(newNode);
        newNode.setPrevNode(previousNode);
    }

    public Node updateNode(Node node) {
        return nodeRepository.save(node);
    }

    public Node getNode(UUID id) {
        return nodeRepository.findById(id)
                .orElseThrow(() -> new NodeNotFoundException(String.format("Node with id %s not found", id)));
    }
}
