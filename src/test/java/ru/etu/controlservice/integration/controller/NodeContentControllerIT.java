package ru.etu.controlservice.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.etu.controlservice.integration.TestContainersConfig;
import ru.etu.controlservice.service.NodeService;
import ru.etu.controlservice.service.PatientService;
import ru.etu.controlservice.service.TreatmentCaseService;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.etu.controlservice.util.NodeTestUtils.*;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NodeContentControllerIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientService patientService;

    @Autowired
    private TreatmentCaseService treatmentCaseService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void getSegmentationCtNode_ShouldReturnCtDto_WhenExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID ctNodeId = createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/ct/{nodeId}", patientId, caseId, ctNodeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("ctOriginalId").isNotEmpty())
                .andExpect(jsonPath("ctMaskId").isNotEmpty());
    }

    @Test
    void getSegmentationJawNode_ShouldReturnJawDto_WhenExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID jawNodeId = createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/jaw/{nodeId}", patientId, caseId, jawNodeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("jawUpperId").isNotEmpty())
                .andExpect(jsonPath("jawLowerId").isNotEmpty())
                .andExpect(jsonPath("jawsSegmented").isNotEmpty());
    }

    @Test
    void getAlignmentNode_ShouldReturnAlignmentDto_WhenExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID alignmentNodeId = createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/alignment/{nodeId}", patientId, caseId, alignmentNodeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("toothRefs").isArray())
                .andExpect(jsonPath("toothRefs.length()").value(1))
                .andExpect(jsonPath("initTeethMatrices").isArray())
                .andExpect(jsonPath("initTeethMatrices.length()").value(1));
    }

    @Test
    void getResultNode_ShouldReturnResultDto_WhenExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID resultNodeId = createResultPlanningNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/planning/result/{nodeId}", patientId, caseId, resultNodeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("desiredTeethMatrices").isArray())
                .andExpect(jsonPath("desiredTeethMatrices.length()").value(1));
    }

    @Test
    void getTreatmentNode_ShouldReturnTreatmentDto_WhenExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID treatmentNodeId = createTreatmentStepNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/planning/treatment/{nodeId}", patientId, caseId, treatmentNodeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("treatmentStepMatrixGroup").isArray())
                .andExpect(jsonPath("treatmentStepMatrixGroup.length()").value(1))
                .andExpect(jsonPath("attachment").isArray())
                .andExpect(jsonPath("attachment.length()").value(1));
    }

    @Test
    void getSegmentationCtNode_ShouldReturn404_WhenNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID ctNodeId = createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/ct/{nodeId}", patientId, caseId, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getSegmentationJawNode_ShouldReturn404_WhenNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID jawNodeId = createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/jaw/{nodeId}", patientId, caseId, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAlignmentNode_ShouldReturn404Dto_WhenNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID alignmentNodeId = createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/alignment/{nodeId}", patientId, caseId, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getResultNode_ShouldReturn404_WhenNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID resultNodeId = createResultPlanningNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/planning/result/{nodeId}", patientId, caseId, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getTreatmentNode_ShouldReturn404_WhenNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID treatmentNodeId = createTreatmentStepNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/planning/treatment/{nodeId}", patientId, caseId, UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getSegmentationCtNode_ShouldReturn400_WhenNodeHaveDifferentType() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID alignmentNodeId = createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/ct/{nodeId}", patientId, caseId, alignmentNodeId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getSegmentationJawNode_ShouldReturn400_WhenNodeHaveDifferentType() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID alignmentNodeId = createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/jaw/{nodeId}", patientId, caseId, alignmentNodeId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getAlignmentNode_ShouldReturn400Dto_WhenNodeHaveDifferentType() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID treatmentNodeId = createTreatmentStepNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/segmentation/alignment/{nodeId}", patientId, caseId, treatmentNodeId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getResultNode_ShouldReturn400_WhenNodeHaveDifferentType() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID ctNodeId = createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/planning/result/{nodeId}", patientId, caseId, ctNodeId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getTreatmentNode_ShouldReturn400_WhenNodeHaveDifferentType() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        UUID ctNodeId = createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/planning/treatment/{nodeId}", patientId, caseId, ctNodeId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

}
