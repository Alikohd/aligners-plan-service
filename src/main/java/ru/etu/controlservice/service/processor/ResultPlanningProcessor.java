package ru.etu.controlservice.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.ResultPlanningPayload;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.service.ResultPlanningClient;
import ru.etu.controlservice.service.SegmentationNodeUpdater;
import ru.etu.grpc.segmentation.AnatomicalStructure;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResultPlanningProcessor implements TaskProcessor {
    private final ResultPlanningClient resultPlanningClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final NodeRepository nodeRepository;

    @Override
    public void process(Object payload, Node node) {
        try {
            ResultPlanningPayload resultPlanningPayload = (ResultPlanningPayload) payload;
            UUID alignmentNodeId = resultPlanningPayload.alignmentNodeId();

            log.info("Processing RESULT_PLANNING for node {}: alignmentNodeId={}", node.getId(), alignmentNodeId);

            Node alignmentSegmentationNode = nodeRepository.findByIdWithAlignmentSegmentation(alignmentNodeId)
                    .orElseThrow(() -> new IllegalStateException("AlignmentSegmentation node not found: " + alignmentNodeId));

            AlignmentSegmentation alignmentSegmentation = alignmentSegmentationNode.getAlignmentSegmentation();
            if (alignmentSegmentation == null) {
                throw new IllegalStateException("AlignmentSegmentation not found for node " + alignmentNodeId);
            }

            List<String> stls = alignmentSegmentation.getStlToothRefs();
            List<String> initTeethMatrices = alignmentSegmentation.getInitTeethMatrices();

            List<AnatomicalStructure> anatomicalStructures = IntStream.range(0, stls.size())
                    .mapToObj(i -> AnatomicalStructure.newBuilder()
                            .setStl(stls.get(i))
                            .setInitMatrix(initTeethMatrices.get(i))
                            .build())
                    .toList();

            List<String> desiredTeethMatrices = resultPlanningClient.planResult(anatomicalStructures);
            if (desiredTeethMatrices == null) {
                log.error("ResultPlanning returned null for node {}", node.getId());
                throw new RuntimeException("ResultPlanning returned null result");
            }

            segmentationNodeUpdater.setResultPlanning(node, desiredTeethMatrices);
        } catch (Exception e) {
            log.error("Failed to process RESULT_PLANNING task for node {}: {}", node.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process RESULT_PLANNING task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.RESULT_PLANNING;
    }
}