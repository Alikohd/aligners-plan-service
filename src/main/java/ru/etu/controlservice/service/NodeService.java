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
    public Node createStep(TreatmentCase treatmentCase){
        Node node = treatmentCase.getRoot();
        if (node == null){
            node = nodeRepository.save(Node.builder()
                            .treatmentBranchId(1L)
                    .build());
            treatmentCase.setRoot(node);
            treatmentCaseRepository.save(treatmentCase);
            return node;
        }
        while (!node.getNextNodes().isEmpty()){
            node = node.getNextNodes().stream()
                    .max(Comparator.comparing(nodeNextRelation -> nodeNextRelation.getNextNode().getTreatmentBranchId()))
                    .get().getNode();
        }
        Node lastNode = Node.builder()
                        .treatmentBranchId(node.getTreatmentBranchId())
                .build();
        node.getNextNodes().add(NodeNextRelation.builder()
                        .node(node)
                        .nextNode(lastNode)
                .build());
        lastNode.getPrevNodes().add(NodePrevRelation.builder()
                        .node(lastNode)
                        .prevNode(node)
                .build());
        nodeRepository.save(node);
        return nodeRepository.save(lastNode);
    }

}
