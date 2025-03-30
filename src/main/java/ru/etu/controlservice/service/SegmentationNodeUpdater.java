package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.repository.NodeRepository;

import java.util.List;

/**
 * Service used for updating segmentation nodes within a transactional context to avoid self injection in SegmentationService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SegmentationNodeUpdater {
    private final NodeRepository nodeRepository;

    @Transactional
    public void updateNodesWithSegmentation(Node ctNode, Node jawNode,
                                            String ctOriginal, String jawUpperStlSaved, String jawLowerStlSaved,
                                            String ctSegmentationResponse, List<String> jawSegmentationResponse) {
        setCtSegmentation(ctNode, ctOriginal, ctSegmentationResponse);
        setJawSegmentation(jawNode, jawUpperStlSaved, jawLowerStlSaved, jawSegmentationResponse);
        nodeRepository.save(ctNode);
        nodeRepository.save(jawNode);
    }

    public void setCtSegmentation(Node node, String ctOriginal, String ctMask) {
        log.debug("Setting CtSegmentation: ctOriginal = {}, ctMask = {}", ctOriginal, ctMask);
        CtSegmentation ctSegmentation = CtSegmentation.builder()
                .ctOriginal(ctOriginal)
                .ctMask(ctMask)
                .build();
        node.setCtSegmentation(ctSegmentation);
    }

    public void setJawSegmentation(Node node, String jawUpperStl, String jawLowerStl, List<String> jawsJson) {
        log.debug("Setting JawSegmentation: jawUpperStl = {}, jawLowerStl = {}, jawsJson = {}", jawUpperStl, jawLowerStl, jawsJson);
        JawSegmentation jawSegmentation = JawSegmentation.builder()
                .jawUpperStl(jawUpperStl)
                .jawLowerStl(jawLowerStl)
                .jawsJson(jawsJson)
                .build();
        node.setJawSegmentation(jawSegmentation);
    }
}
