package ru.etu.controlservice.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.AlignmentPayload;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
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

    @Override
    public void process(Object payload, Node node) {
        try {
            AlignmentPayload alignmentPayload = (AlignmentPayload) payload;
            UUID ctNodeId = alignmentPayload.ctNodeId();
            UUID jawNodeId = alignmentPayload.jawNodeId();

            log.info("Processing SEGMENTATION_ALIGNMENT for node {}: ctNodeId={}, jawNodeId={}",
                    node.getId(), ctNodeId, jawNodeId);

            CtSegmentation ctSegmentation = nodeRepository.findByIdWithCtSegmentation(ctNodeId)
                    .orElseThrow(() -> new IllegalStateException("CtSegmentation not found for node " + ctNodeId))
                    .getCtSegmentation();
            JawSegmentation jawSegmentation = nodeRepository.findByIdWithJawSegmentation(jawNodeId)
                    .orElseThrow(() -> new IllegalStateException("JawSegmentation not found for node " + jawNodeId))
                    .getJawSegmentation();

            if (ctSegmentation == null || jawSegmentation == null) {
                throw new IllegalStateException("Required segmentations not found: ctSegmentation=" +
                        (ctSegmentation == null ? "null" : "present") +
                        ", jawSegmentation=" +
                        (jawSegmentation == null ? "null" : "present"));
            }

            List<AnatomicalStructure> alignmentSegmentationResponse = segmentationClient.align(
                    ctSegmentation.getCtMask(),
                    jawSegmentation.getJawUpperStl().getStorageIdentifier(),
                    jawSegmentation.getJawLowerStl().getStorageIdentifier(),
                    jawSegmentation.getJawsJson()
            );

            if (alignmentSegmentationResponse == null) {
                log.error("Alignment returned null for node {}", node.getId());
                throw new RuntimeException("Alignment returned null result");
            }

            List<String> stls = alignmentSegmentationResponse.stream()
                    .map(AnatomicalStructure::getStl)
                    .toList();
            List<String> initMatrices = alignmentSegmentationResponse.stream()
                    .map(AnatomicalStructure::getInitMatrix)
                    .toList();

            segmentationNodeUpdater.setAlignmentSegmentation(node, stls, initMatrices);
        } catch (Exception e) {
            log.error("Failed to process SEGMENTATION_ALIGNMENT task for node {}: {}", node.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process SEGMENTATION_ALIGNMENT task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.SEGMENTATION_ALIGNMENT;
    }
}