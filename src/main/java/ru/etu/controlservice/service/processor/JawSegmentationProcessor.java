package ru.etu.controlservice.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.SegmentationJawPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.service.SegmentationClient;
import ru.etu.controlservice.service.SegmentationNodeUpdater;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JawSegmentationProcessor implements TaskProcessor {
    private final SegmentationClient segmentationClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;

    @Override
    public void process(Object payload, Node node) {
        try {
            SegmentationJawPayload jawPayload = (SegmentationJawPayload) payload;
            String jawUpperStlSaved = jawPayload.jawUpperStl();
            String jawLowerStlSaved = jawPayload.jawLowerStl();
            log.info("Processing SEGMENTATION_JAW for node {}: upperStl={}, lowerStl={}",
                    node.getId(), jawUpperStlSaved, jawLowerStlSaved);

            List<String> jawsJson = segmentationClient.segmentJaw(jawUpperStlSaved, jawLowerStlSaved);
            segmentationNodeUpdater.updateJawSegmentation(node, jawUpperStlSaved, jawLowerStlSaved, jawsJson);
        } catch (Exception e) {
            log.error("Failed to process SEGMENTATION_JAW task for node {}: {}", node.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process SEGMENTATION_JAW task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.SEGMENTATION_JAW;
    }
}