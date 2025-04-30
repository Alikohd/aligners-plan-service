package ru.etu.controlservice.util;

import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.exceptions.NodeNotFoundException;
import ru.etu.controlservice.exceptions.RequiredNodesNotFoundException;
import ru.etu.controlservice.repository.NodeRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class NodeContentUtils {
    private final NodeRepository nodeRepository;

    public static NodeType getNodeType(Node node) {
        return Arrays.stream(NodeType.values())
                .filter(type -> type.test(node))
                .findFirst()
                .orElse(NodeType.EMPTY_NODE);
    }

    public Map<NodeType, Node> getRequiredPrevNodes(Node backTraceNode, List<NodeType> requiredNodes) {
        Map<NodeType, Node> prevNodesMap = Stream.iterate(backTraceNode,
                        node -> Objects.nonNull(node.getPrevNode()),
                        Node::getPrevNode)
                .limit(requiredNodes.size())
                .flatMap(node -> requiredNodes.stream()
                        .filter(type -> type.getNodeStep(node) != null)
                        .map(type -> Map.entry(type, node)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (prevNodesMap.size() != requiredNodes.size()) {
            throw new RequiredNodesNotFoundException(String.format("Operation requires nodes %s that haven't been found", requiredNodes));
        }
        return prevNodesMap;
    }

    @Transactional(readOnly = true)
    public Node getNodeWithType(NodeType nodeType, UUID backTraceNodeId) {
        Node backTraceNode = nodeRepository.findById(backTraceNodeId)
                .orElseThrow(() -> new NodeNotFoundException(String.format("Node with id %s not found", backTraceNodeId)));

        Node result = Stream.iterate(backTraceNode,
                        node -> Objects.nonNull(node.getPrevNode()),
                        Node::getPrevNode)
                .filter(node -> nodeType.getNodeStep(node) != null)
                .findFirst().orElseThrow(() ->
                        new NodeNotFoundException(String.format("Node with type %s not found", nodeType.name())));

        Object nodeContent = nodeType.getNodeStep(result);
        if (nodeContent != null) {
            Hibernate.initialize(nodeContent);
        }
        return result;
    }
}
