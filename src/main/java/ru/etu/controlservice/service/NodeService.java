package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.NodeNotFoundException;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.repository.TreatmentCaseRepository;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class NodeService {

    private final NodeRepository nodeRepository;
    private final TreatmentCaseRepository treatmentCaseRepository;

    @Transactional(propagation = Propagation.MANDATORY)
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

    private Node findLastNode(Node startNode) {
        return traverseNodes(startNode)
                .reduce((first, second) -> second) // Берем последний элемент потока
                .orElse(startNode); // Если поток пустой, возвращаем начальный узел
    }

    public Stream<Node> traverseNodes(Node startNode) {
        return Stream.iterate(
                startNode,
                Objects::nonNull,
                node -> node.getNextNodes().stream()
                        .max(Comparator.comparing(Node::getCreatedAt))
                        .orElse(null)
        );
    }

    @Transactional
    protected Node addStepTo(Node previousNode) {
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
