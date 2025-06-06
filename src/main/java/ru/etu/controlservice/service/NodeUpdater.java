package ru.etu.controlservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.Struct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import ru.etu.controlservice.repository.JawSegRepository;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.repository.ResultPlanningRepository;
import ru.etu.controlservice.repository.TreatmentPlanningRepository;
import ru.etu.controlservice.util.ProtobufUtils;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NodeUpdater {
    private final NodeRepository nodeRepository;
    private final CtSegRepository ctSegRepository;
    private final JawSegRepository jawSegRepository;
    private final AlignmentSegRepository alignmentSegRepository;
    private final ResultPlanningRepository resultPlanningRepository;
    private final TreatmentPlanningRepository treatmentPlanningRepository;
    private final NodeService nodeService;

    @Transactional
    public void updateCtSegmentation(Node node, String ctOriginal, String ctMask) {
        log.debug("Транзакция активна: {}", TransactionSynchronizationManager.isActualTransactionActive());
        File ctOriginalFile = File.fromPacs(ctOriginal);
        File ctMaskFile = File.fromPacs(ctMask);
        CtSegmentation ctSegmentation = CtSegmentation.builder()
                .ctOriginal(ctOriginalFile)
                .ctMask(ctMaskFile)
                .build();
        ctSegRepository.saveAndFlush(ctSegmentation);
        node.setCtSegmentation(ctSegmentation);
        nodeRepository.saveAndFlush(node);
    }

    @Transactional
    public void updateJawSegmentation(Node node, String jawUpperStl, String jawLowerStl, List<JsonNode> jawsJson) {
        log.debug("Setting JawSegmentation: jawUpperStl = {}, jawLowerStl = {}, jawsJson = {}", jawUpperStl, jawLowerStl, jawsJson);
        File jawUpperFile = File.fromS3(jawUpperStl);
        File jawLowerFile = File.fromS3(jawLowerStl);
        JawSegmentation jawSegmentation = JawSegmentation.builder()
                .jawUpper(jawUpperFile)
                .jawLower(jawLowerFile)
                .jawsSegmented(jawsJson)
                .build();
        jawSegRepository.save(jawSegmentation);
        node.setJawSegmentation(jawSegmentation);
        nodeRepository.save(node);
    }

    @Transactional
    public void setAlignmentSegmentation(Node node,
                                         List<String> stlToothRefs, List<Struct> initTeethMatrices) {
        log.debug("Setting Alignment...");
        List<JsonNode> jawsSegmented = ProtobufUtils.structsToJsonNodes(initTeethMatrices);
        List<File> toothRefFiles = stlToothRefs.stream().map(File::fromS3).toList();
        AlignmentSegmentation alignmentSegmentation = AlignmentSegmentation.builder()
                .initTeethMatrices(jawsSegmented)
                .toothRefs(toothRefFiles)
                .build();
        alignmentSegRepository.save(alignmentSegmentation);
        node.setAlignmentSegmentation(alignmentSegmentation);
        nodeRepository.save(node);
    }

    @Transactional
    public void setResultPlanning(Node node, List<JsonNode> desiredTeethMatrices) {
        log.debug("Setting ResultPlanning...");
        ResultPlanning resultPlanning = ResultPlanning.builder()
                .desiredTeethMatrices(desiredTeethMatrices)
                .build();
        resultPlanningRepository.save(resultPlanning);
        node.setResultPlanning(resultPlanning);
        nodeRepository.save(node);
    }

    @Transactional
    public void setTreatmentPlanning(Node lastNode, List<JsonNode> matrixGroups,
                                     List<JsonNode> attachments) {
        log.debug("Setting TreatmentPlanning steps...");

        if (matrixGroups.size() != attachments.size()) {
            throw new IllegalArgumentException("Mismatch between matrix groups and attachment size");
        }
        TreatmentPlanning firstPlanning = TreatmentPlanning.builder()
                .treatmentStepMatrixGroup(matrixGroups.get(0))
                .attachment(attachments.get(0))
                .build();
        treatmentPlanningRepository.save(firstPlanning);
        lastNode.setTreatmentPlanning(firstPlanning);
        nodeRepository.save(lastNode);

        Node currNode = lastNode;
        for (int i = 1; i < matrixGroups.size(); i++) {
            TreatmentPlanning planningStep = TreatmentPlanning.builder()
                    .treatmentStepMatrixGroup(matrixGroups.get(i))
                    .attachment(attachments.get(i))
                    .build();
            treatmentPlanningRepository.save(planningStep);

            Node node = nodeService.addStepTo(currNode);
            node.setTreatmentPlanning(planningStep);
            nodeRepository.save(node);
            currNode = node;
        }
    }
}
