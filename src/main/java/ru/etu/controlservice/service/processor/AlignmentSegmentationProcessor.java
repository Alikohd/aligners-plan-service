package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.AlignmentPayload;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.service.SegmentationClient;
import ru.etu.controlservice.service.SegmentationNodeUpdater;
import ru.etu.grpc.segmentation.AnatomicalStructure;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlignmentSegmentationProcessor implements TaskProcessor {
    private final SegmentationClient segmentationClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final NodeRepository nodeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Task task) {
        try {
            AlignmentPayload payload = objectMapper.readValue(task.getPayload(), AlignmentPayload.class);
            UUID ctNodeId = payload.ctNodeId();
            UUID jawNodeId = payload.jawNodeId();

            Node alignmentNode = task.getNode();
            if (alignmentNode == null) {
                throw new IllegalStateException("No Node associated with task " + task.getId());
            }

            CtSegmentation ctSegmentation = nodeRepository.findByIdWithCtSegmentation(ctNodeId).getCtSegmentation();
            JawSegmentation jawSegmentation = nodeRepository.findByIdWithJawSegmentation(jawNodeId).getJawSegmentation();

            log.info("Task {}: ctSegmentation is null: {}, jawSegmentation is null: {}",
                    task.getId(), ctSegmentation == null, jawSegmentation == null);

            if (ctSegmentation == null || jawSegmentation == null) {
                throw new IllegalStateException("Required segmentations not found: ctSegmentation=" +
                        (ctSegmentation == null ? "null" : "present") +
                        ", jawSegmentation=" +
                        (jawSegmentation == null ? "null" : "present"));
            }

            List<AnatomicalStructure> alignmentSegmentationResponse = segmentationClient.align(
                    ctSegmentation.getCtMask(),
                    jawSegmentation.getJawUpperStl(),
                    jawSegmentation.getJawLowerStl(),
                    jawSegmentation.getJawsJson()
            );

            if (alignmentSegmentationResponse == null) {
                log.error("Alignment returned null for task {}", task.getId());
                throw new RuntimeException("Alignment returned null result");
            }

            List<String> stls = alignmentSegmentationResponse.stream()
                    .map(AnatomicalStructure::getStl)
                    .toList();
            List<String> initMatrices = alignmentSegmentationResponse.stream()
                    .map(AnatomicalStructure::getInitMatrix)
                    .toList();

            segmentationNodeUpdater.setAlignmentSegmentation(
                    alignmentNode, ctSegmentation, jawSegmentation, stls, initMatrices
            );
        } catch (Exception e) {
            log.error("Alignment failed for task {}: {}", task.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process ALIGNMENT task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.SEGMENTATION_ALIGNMENT;
    }

}