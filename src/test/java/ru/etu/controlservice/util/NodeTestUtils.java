package ru.etu.controlservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
    private final static ObjectMapper mapper = new ObjectMapper();

    public static UUID createCtSegmentationNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeCt = nodeService.addStepToEnd(treatmentCase);
        File testCtFile = File.fromPacs(SegmentationTestData.mockCtOriginalUri);
        File testMaskFile = File.fromPacs(SegmentationTestData.mockCtMaskUri);
        CtSegmentation ct = CtSegmentation.builder()
                .ctOriginal(testCtFile)
                .ctMask(testMaskFile)
                .build();
        nodeCt.setCtSegmentation(ct);
        nodeService.updateNode(nodeCt);
        return nodeCt.getId();
    }

    @SneakyThrows
    public static UUID createJawSegmentationNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeJaw = nodeService.addStepToEnd(treatmentCase);
        File testJawFile1 = File.fromS3(SegmentationTestData.mockGeneralUri);
        File testJawFile2 = File.fromS3(SegmentationTestData.mockGeneralUri);
        List<JsonNode> jawsSegmented = SegmentationTestData.getJsonNodes(SegmentationTestData.mockJawsSegmented);
        JawSegmentation jaw = JawSegmentation.builder()
                .jawLower(testJawFile1)
                .jawUpper(testJawFile2)
                .jawsSegmented(jawsSegmented)
                .build();
        nodeJaw.setJawSegmentation(jaw);
        nodeService.updateNode(nodeJaw);
        return nodeJaw.getId();
    }

    public static UUID createAlignmentSegmentationNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeAlignment = nodeService.addStepToEnd(treatmentCase);
        File testStlFile = File.fromS3(SegmentationTestData.mockGeneralUri);
        JsonNode jsonNode = SegmentationTestData.getMockJson();
        AlignmentSegmentation alignment = AlignmentSegmentation.builder()
                .toothRefs(List.of(testStlFile))
                .initTeethMatrices(List.of(jsonNode))
                .build();
        nodeAlignment.setAlignmentSegmentation(alignment);
        nodeService.updateNode(nodeAlignment);
        return nodeAlignment.getId();
    }

    public static UUID createResultPlanningNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeResult = nodeService.addStepToEnd(treatmentCase);
        JsonNode jsonNode = SegmentationTestData.getMockJson();
        ResultPlanning result = ResultPlanning.builder().desiredTeethMatrices(List.of(jsonNode)).build();
        nodeResult.setResultPlanning(result);
        nodeService.updateNode(nodeResult);
        return nodeResult.getId();
    }

    public static UUID createTreatmentStepNode(UUID patientId, UUID caseId, TreatmentCaseService treatmentCaseService, NodeService nodeService) {
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeTreatment = nodeService.addStepToEnd(treatmentCase);
        JsonNode jsonNode = SegmentationTestData.getMockJson();
        TreatmentPlanning treatmentStep = TreatmentPlanning.builder().treatmentStepMatrixGroup(jsonNode).attachment(SegmentationTestData.getMockJson()).build();
        nodeTreatment.setTreatmentPlanning(treatmentStep);
        nodeService.updateNode(nodeTreatment);
        return nodeTreatment.getId();
    }
}
