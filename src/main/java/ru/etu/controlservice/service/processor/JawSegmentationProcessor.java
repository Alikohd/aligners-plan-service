package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.SegmentationJawPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.service.SegmentationClient;
import ru.etu.controlservice.service.SegmentationNodeUpdater;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JawSegmentationProcessor implements TaskProcessor {
    private final SegmentationClient segmentationClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Task task) {
        try {
            SegmentationJawPayload payload = objectMapper.readValue(task.getPayload(), SegmentationJawPayload.class);
            String jawUpperStlSaved = payload.jawUpperStl();
            String jawLowerStlSaved = payload.jawLowerStl();
            Node jawNode = task.getNode();
            System.out.println("Processing SEGMENTATION_JAW: " + jawLowerStlSaved + jawUpperStlSaved);
            List<String> jawsJson = segmentationClient.segmentJaw(jawUpperStlSaved, jawLowerStlSaved);

            segmentationNodeUpdater.updateJawSegmentation(jawNode, jawUpperStlSaved, jawLowerStlSaved, jawsJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process SEGMENTATION_JAW task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.SEGMENTATION_JAW;
    }
}
