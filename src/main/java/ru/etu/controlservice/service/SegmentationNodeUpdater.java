package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.ResultPlanning;
import ru.etu.controlservice.repository.AlignmentSegRepository;
import ru.etu.controlservice.repository.CtSegRepository;
import ru.etu.controlservice.repository.JawSegRepository;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.repository.ResultPlanningRepository;

import java.util.List;

/**
 * Service used for updating segmentation nodes within a transactional context to avoid self injection in SegmentationService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SegmentationNodeUpdater {
    private final NodeRepository nodeRepository;
    private final CtSegRepository ctSegRepository;
    private final JawSegRepository jawSegRepository;
    private final AlignmentSegRepository alignmentSegRepository;
    private final ResultPlanningRepository resultPlanningRepository;

    @Transactional
    public void updateCtSegmentation(Node node, String ctOriginal, String ctMask) {
        CtSegmentation ctSegmentation = CtSegmentation.builder()
                .ctOriginal(ctOriginal)
                .ctMask(ctMask)
                .build();
        ctSegRepository.save(ctSegmentation);
        node.setCtSegmentation(ctSegmentation);
        nodeRepository.save(node);
    }

    @Transactional
    public void updateJawSegmentation(Node node, String jawUpperStl, String jawLowerStl, List<String> jawsJson) {
        log.debug("Setting JawSegmentation: jawUpperStl = {}, jawLowerStl = {}, jawsJson = {}", jawUpperStl, jawLowerStl, jawsJson);
        JawSegmentation jawSegmentation = JawSegmentation.builder()
                .jawUpperStl(jawUpperStl)
                .jawLowerStl(jawLowerStl)
                .jawsJson(jawsJson)
                .build();
        jawSegRepository.save(jawSegmentation);
        node.setJawSegmentation(jawSegmentation);
        nodeRepository.save(node);
    }

    @Transactional
    public void setAlignmentSegmentation(Node node, CtSegmentation ctSegmentation, JawSegmentation jawSegmentation,
                                         List<String> stlToothRefs, List<String> initTeethMatrices) {
        log.debug("Setting Alignment...");
        AlignmentSegmentation alignmentSegmentation = AlignmentSegmentation.builder()
                .ctSegmentation(ctSegmentation)
                .jawSegmentation(jawSegmentation)
                .initTeethMatrices(initTeethMatrices)
                .stlToothRefs(stlToothRefs)
                .build();
        alignmentSegRepository.save(alignmentSegmentation);
        node.setAlignmentSegmentation(alignmentSegmentation);
        nodeRepository.save(node);
    }

    @Transactional
    public void setResultPlanning(Node node, AlignmentSegmentation alignmentSegmentation, List<String> desiredTeethMatrices) {
        log.debug("Setting ResultPlanning...");
        ResultPlanning resultPlanning = ResultPlanning.builder()
                .alignmentSegmentation(alignmentSegmentation)
                .desiredTeethMatrices(desiredTeethMatrices)
                .build();
        resultPlanningRepository.save(resultPlanning);
        node.setResultPlanning(resultPlanning);
        nodeRepository.save(node);
    }
}
