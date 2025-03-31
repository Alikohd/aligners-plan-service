package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.SegmentationCtPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.service.SegmentationClient;
import ru.etu.controlservice.service.SegmentationNodeUpdater;

@Component
@RequiredArgsConstructor
public class CtSegmentationProcessor implements TaskProcessor {
    private final SegmentationClient segmentationClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Task task) {
        try {
            SegmentationCtPayload payload = objectMapper.readValue(task.getPayload(), SegmentationCtPayload.class);
            String ctOriginal = payload.ctOriginal();
            Node ctNode = task.getNode();
            System.out.println("Processing SEGMENTATION_CT: " + ctOriginal);
            String ctMask = segmentationClient.segmentCt(ctOriginal);
            segmentationNodeUpdater.updateCtSegmentation(ctNode, ctOriginal, ctMask);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process SEGMENTATION_CT task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.SEGMENTATION_CT;
    }
}