package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.SegmentationJawPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.service.SegmentationClient;
import ru.etu.controlservice.service.SegmentationNodeUpdater;
import ru.etu.controlservice.util.ProtobufUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JawSegmentationProcessor implements TaskProcessor {
    private final SegmentationClient segmentationClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final ObjectMapper mapper;

    @Override
    public void process(Object payload, Node node) {
        try {
            SegmentationJawPayload jawPayload = (SegmentationJawPayload) payload;
            String jawUpperStlSaved = jawPayload.jawUpperStl();
            String jawLowerStlSaved = jawPayload.jawLowerStl();
            log.info("Processing SEGMENTATION_JAW for node {}: upperStl={}, lowerStl={}",
                    node.getId(), jawUpperStlSaved, jawLowerStlSaved);
            List<Struct> jawsStructs = segmentationClient.segmentJaw(jawUpperStlSaved, jawLowerStlSaved);
            List<JsonNode> jawsSegmented = ProtobufUtils.structsToJsonNodes(jawsStructs);
            segmentationNodeUpdater.updateJawSegmentation(node, jawUpperStlSaved, jawLowerStlSaved, jawsSegmented);
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