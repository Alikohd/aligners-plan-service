package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.ResultPlanning;
import ru.etu.controlservice.entity.TreatmentPlanning;
import ru.etu.controlservice.repository.AlignmentSegRepository;
import ru.etu.controlservice.repository.CtSegRepository;
import ru.etu.controlservice.repository.FileRepository;
import ru.etu.controlservice.repository.JawSegRepository;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.repository.ResultPlanningRepository;
import ru.etu.controlservice.repository.TreatmentPlanningRepository;

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
    private final TreatmentPlanningRepository treatmentPlanningRepository;
    private final FileRepository fileRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateCtSegmentation(Node node, String ctOriginal, String ctMask) {
        log.debug("Транзакция активна: {}", TransactionSynchronizationManager.isActualTransactionActive());
        CtSegmentation ctSegmentation = CtSegmentation.builder()
                .ctOriginal(ctOriginal)
                .ctMask(ctMask)
                .build();
        ctSegRepository.saveAndFlush(ctSegmentation);
        node.setCtSegmentation(ctSegmentation);
        nodeRepository.saveAndFlush(node);
    }

    @Transactional
    public void updateJawSegmentation(Node node, String jawUpperStl, String jawLowerStl, List<String> jawsJson) {
        log.debug("Setting JawSegmentation: jawUpperStl = {}, jawLowerStl = {}, jawsJson = {}", jawUpperStl, jawLowerStl, jawsJson);
        File jawUpperFile = fileRepository.save(new File(jawUpperStl));
        File jawLowerFile = fileRepository.save(new File(jawLowerStl));
        JawSegmentation jawSegmentation = JawSegmentation.builder()
                .jawUpperStl(jawUpperFile)
                .jawLowerStl(jawLowerFile)
                .jawsJson(jawsJson)
                .build();
        jawSegRepository.save(jawSegmentation);
        node.setJawSegmentation(jawSegmentation);
        nodeRepository.save(node);
    }

    @Transactional
    public void setAlignmentSegmentation(Node node,
                                         List<String> stlToothRefs, List<String> initTeethMatrices) {
        log.debug("Setting Alignment...");
        AlignmentSegmentation alignmentSegmentation = AlignmentSegmentation.builder()
                .initTeethMatrices(initTeethMatrices)
                .stlToothRefs(stlToothRefs)
                .build();
        alignmentSegRepository.save(alignmentSegmentation);
        node.setAlignmentSegmentation(alignmentSegmentation);
        nodeRepository.save(node);
    }

    @Transactional
    public void setResultPlanning(Node node, List<String> desiredTeethMatrices) {
        log.debug("Setting ResultPlanning...");
        ResultPlanning resultPlanning = ResultPlanning.builder()
                .desiredTeethMatrices(desiredTeethMatrices)
                .build();
        resultPlanningRepository.save(resultPlanning);
        node.setResultPlanning(resultPlanning);
        nodeRepository.save(node);
    }

    @Transactional
    public void setTreatmentPlanning(Node node,
                                     List<String> collectionOfMatricesGroups, List<String> attachments) {
        log.debug("Setting TreatmentPlanning...");
        TreatmentPlanning treatmentPlanning = TreatmentPlanning.builder()
                .collectionsOfMatricesGroups(collectionOfMatricesGroups)
                .attachments(attachments)
                .build();
        treatmentPlanningRepository.save(treatmentPlanning);
        node.setTreatmentPlanning(treatmentPlanning);
        nodeRepository.save(node);
    }
}
