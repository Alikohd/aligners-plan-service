package ru.etu.controlservice.service.processor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.SegmentationCtPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.service.SegmentationNodeUpdater;
import ru.etu.controlservice.service.client.SegmentationClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class CtSegmentationProcessor implements TaskProcessor {
    private final SegmentationClient segmentationClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;

    @PostConstruct
    public void checkProxy() {
        log.info("SegmentationNodeUpdater является прокси: {}", AopUtils.isAopProxy(segmentationNodeUpdater));
    }

    @Override
    public void process(Object payload, Node node) {
        try {
            SegmentationCtPayload ctPayload = (SegmentationCtPayload) payload;
            String ctOriginal = ctPayload.ctOriginal();
            log.info("Processing SEGMENTATION_CT for node {}: ctOriginal={}", node.getId(), ctOriginal);
            String ctMask = segmentationClient.segmentCt(ctOriginal);
            segmentationNodeUpdater.updateCtSegmentation(node, ctOriginal, ctMask);
            log.debug("test");
        } catch (Exception e) {
            log.error("Failed to process SEGMENTATION_CT task for node {}: {}", node.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process SEGMENTATION_CT task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.SEGMENTATION_CT;
    }
}