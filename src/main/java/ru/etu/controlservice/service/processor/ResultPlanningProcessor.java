package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.Struct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.ResultPlanningPayload;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.service.NodeUpdater;
import ru.etu.controlservice.service.client.ResultPlanningClient;
import ru.etu.controlservice.util.ProtobufUtils;
import ru.etu.grpc.segmentation.AnatomicalStructure;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResultPlanningProcessor implements TaskProcessor {
    private final ResultPlanningClient resultPlanningClient;
    private final NodeUpdater nodeUpdater;
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

            List<String> stls = alignmentSegmentation.getToothRefs().stream().map(File::getUri).toList();
            List<JsonNode> initTeethMatrices = alignmentSegmentation.getInitTeethMatrices();
            List<Struct> structsMatrices = ProtobufUtils.jsonNodesToStructs(initTeethMatrices);

            List<AnatomicalStructure> anatomicalStructures = IntStream.range(0, stls.size())
                    .mapToObj(i -> AnatomicalStructure.newBuilder()
                            .setStl(stls.get(i))
                            .setInitMatrix(structsMatrices.get(i))
                            .build())
                    .toList();

            List<Struct> desiredTeethMatricesStructs = resultPlanningClient.planResult(anatomicalStructures);
            if (desiredTeethMatricesStructs == null) {
                log.error("ResultPlanning returned null for node {}", node.getId());
                throw new RuntimeException("ResultPlanning returned null result");
            }

            List<JsonNode> desiredTeethMatrices = ProtobufUtils.structsToJsonNodes(desiredTeethMatricesStructs);
            nodeUpdater.setResultPlanning(node, desiredTeethMatrices);
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