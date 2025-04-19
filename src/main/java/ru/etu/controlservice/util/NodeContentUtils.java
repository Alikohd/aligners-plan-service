package ru.etu.controlservice.util;

import org.springframework.stereotype.Component;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class NodeContentUtils {
    public Map<NodeType, Node> getPrevNodes(Node backTraceNode, List<NodeType> requiredNodes) {
        return Stream.iterate(backTraceNode,
                        node -> Objects.nonNull(node.getPrevNode()),
                        Node::getPrevNode)
                .flatMap(node -> requiredNodes.stream()
                        .filter(type -> type.getNodeStep(node) != null)
                        .map(type -> Map.entry(type, node)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
