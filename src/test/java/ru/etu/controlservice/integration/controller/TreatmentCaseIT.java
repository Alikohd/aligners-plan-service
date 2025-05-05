package ru.etu.controlservice.integration.controller;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.integration.TestContainersConfig;
import ru.etu.controlservice.service.NodeService;
import ru.etu.controlservice.service.PatientService;
import ru.etu.controlservice.service.TreatmentCaseService;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.etu.controlservice.util.NodeTestUtils.*;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TreatmentCaseIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientService patientService;

    @Autowired
    private TreatmentCaseService treatmentCaseService;

    @Autowired
    private NodeService nodeService;

    @Test
    void addTreatmentCase_ShouldSaveNewTreatmentCase() throws Exception {
        UUID patientId = patientService.addPatient().id();

        mockMvc.perform(post("/patients/{patientId}/cases", patientId))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").isNotEmpty());
    }

    @Test
    void getTreatmentCase_ShouldReturnCase_IfExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}", patientId, caseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("patientId").isNotEmpty())
                .andExpect(jsonPath("rootId").isNotEmpty())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCases_ShouldReturnAllCases() throws Exception {
        UUID patientId = patientService.addPatient().id();
        treatmentCaseService.createCase(patientId).id();
        treatmentCaseService.createCase(patientId).id();

        mockMvc.perform(get("/patients/{patientId}/cases", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTreatmentPlan_ShouldReturnFlatGraph() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        createResultPlanningNode(patientId, caseId, treatmentCaseService, nodeService);
        createTreatmentStepNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(get("/patients/{patientId}/cases/{caseId}/treatment-plan", patientId, caseId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[1].type").value(NodeType.SEGMENTATION_CT.toString()))
                .andExpect(jsonPath("$[2].type").value(NodeType.SEGMENTATION_JAW.toString()))
                .andExpect(jsonPath("$[3].type").value(NodeType.SEGMENTATION_ALIGNMENT.toString()))
                .andExpect(jsonPath("$[4].type").value(NodeType.RESULT_PLANNING.toString()))
                .andExpect(jsonPath("$[5].type").value(NodeType.TREATMENT_PLANNING.toString()));
    }
}
