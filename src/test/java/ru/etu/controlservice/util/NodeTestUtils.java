package ru.etu.controlservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.ResultPlanning;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.entity.TreatmentPlanning;
import ru.etu.controlservice.service.NodeService;
import ru.etu.controlservice.service.TreatmentCaseService;

import java.util.List;
import java.util.UUID;

public class NodeTestUtils {

    public static void createCtSegmentationNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeCt = nodeService.addStepToEnd(treatmentCase);
        File testCtFile = File.fromPacs("testUri");
        File testMaskFile = File.fromPacs("testUri");
        CtSegmentation ct = CtSegmentation.builder()
                .ctOriginal(testCtFile)
                .ctMask(testMaskFile)
                .build();
        nodeCt.setCtSegmentation(ct);
        nodeService.updateNode(nodeCt);
    }

    public static void createJawSegmentationNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeJaw = nodeService.addStepToEnd(treatmentCase);
        File testJawFile1 = File.fromS3("testUri");
        File testJawFile2 = File.fromS3("testUri");
        JawSegmentation jaw = JawSegmentation.builder()
                .jawLower(testJawFile1)
                .jawUpper(testJawFile2)
                .build();
        nodeJaw.setJawSegmentation(jaw);
        nodeService.updateNode(nodeJaw);
    }

    public static void createAlignmentSegmentationNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeAlignment = nodeService.addStepToEnd(treatmentCase);
        File testStlFile = File.fromS3("testUri");
        JsonNode jsonNode = SegmentationTestData.getMockJson();
        AlignmentSegmentation alignment = AlignmentSegmentation.builder()
                .toothRefs(List.of(testStlFile))
                .initTeethMatrices(List.of(jsonNode))
                .build();
        nodeAlignment.setAlignmentSegmentation(alignment);
        nodeService.updateNode(nodeAlignment);
    }

    public static void createResultPlanningNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeResult = nodeService.addStepToEnd(treatmentCase);
        JsonNode jsonNode = SegmentationTestData.getMockJson();
        ResultPlanning result = ResultPlanning.builder().desiredTeethMatrices(List.of(jsonNode)).build();
        nodeResult.setResultPlanning(result);
        nodeService.updateNode(nodeResult);
    }

    public static void createTreatmentStepNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeTreatment = nodeService.addStepToEnd(treatmentCase);
        JsonNode jsonNode = SegmentationTestData.getMockJson();
        TreatmentPlanning treatmentStep = TreatmentPlanning.builder().treatmentStepMatrixGroup(jsonNode).attachment(SegmentationTestData.getMockJson()).build();
        nodeTreatment.setTreatmentPlanning(treatmentStep);
        nodeService.updateNode(nodeTreatment);
    }
}
