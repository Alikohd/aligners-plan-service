package ru.etu.controlservice.integration.controller;

import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.integration.TestContainersConfig;
import ru.etu.controlservice.service.NodeService;
import ru.etu.controlservice.service.PatientService;
import ru.etu.controlservice.service.TreatmentCaseService;
import ru.etu.controlservice.util.SegmentationTestData;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.etu.controlservice.util.NodeTestUtils.*;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StartModelingIT extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientService patientService;

    @Autowired
    private TreatmentCaseService treatmentCaseService;

    @Autowired
    private NodeService nodeService;

    @Test
    void startCtSegmentation_ShouldAcceptTask_WhenCtArchiveValid() throws Exception {
        MockMultipartFile ctArchive = SegmentationTestData.getDicomArchive();
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/ct", patientId, caseId)
                        .file(ctArchive)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void startCtSegmentation_ShouldAcceptTaskAndLinkToGivenNode_WhenNodeIsNotNull() throws Exception {
        MockMultipartFile ctArchive = SegmentationTestData.getDicomArchive();
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeToLink = nodeService.addStepToEnd(treatmentCase);
        Node lastNode = nodeService.addStepToEnd(treatmentCase);

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/ct", patientId, caseId)
                        .file(ctArchive)
                        .param("node", nodeToLink.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToLink.getId());
    }

    @Test
    void startCtSegmentation_ShouldReturn400_WhenCtArchiveInvalid() throws Exception {
        MockMultipartFile ctArchive = SegmentationTestData.getInvalidDicomArchive();
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/ct", patientId, caseId)
                .file(ctArchive)).andExpect(status().isBadRequest());
    }

    @Test
    void startJawSegmentation_ShouldAcceptTask_WhenStlFilesValid() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        MockMultipartFile jawLower = SegmentationTestData.getStlFile("jawLowerStl");
        MockMultipartFile jawUpper = SegmentationTestData.getStlFile("jawUpperStl");

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/jaw", patientId, caseId)
                        .file(jawLower)
                        .file(jawUpper))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void startJawSegmentation_ShouldAcceptTaskAndLinkToGivenNode_WhenNodeIsNotNull() throws Exception {
        MockMultipartFile jawLower = SegmentationTestData.getInvalidTextStlFile("jawLowerStl");
        MockMultipartFile jawUpper = SegmentationTestData.getStlFile("jawUpperStl");
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeToLink = nodeService.addStepToEnd(treatmentCase);
        Node lastNode = nodeService.addStepToEnd(treatmentCase);

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/jaw", patientId, caseId)
                        .file(jawLower)
                        .file(jawUpper)
                        .param("node", nodeToLink.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToLink.getId());
    }

    @Test
    void startJawSegmentation_ShouldReturn400_WhenStlFilesInvalid() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        MockMultipartFile jawLower = SegmentationTestData.getInvalidTextStlFile("jawLowerStl");
        MockMultipartFile jawUpper = SegmentationTestData.getStlFile("jawUpperStl");

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/jaw", patientId, caseId)
                        .file(jawLower)
                        .file(jawUpper))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void startAlignmentSegmentation_ShouldAcceptTask_WhenRequiredNodesExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/alignment", patientId, caseId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void startAlignment_ShouldAcceptTaskAndLinkToGivenNode_WhenNodeIsNotNull() throws Exception {
        MockMultipartFile ctArchive = SegmentationTestData.getDicomArchive();
        MockMultipartFile jawLower = SegmentationTestData.getInvalidTextStlFile("jawLowerStl");
        MockMultipartFile jawUpper = SegmentationTestData.getStlFile("jawUpperStl");
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        Node lastNode = nodeService.addStepToEnd(treatmentCase);
        UUID nodeToLink = lastNode.getPrevNode().getId();

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/alignment", patientId, caseId)
                        .file(jawLower)
                        .file(jawUpper)
                        .file(ctArchive)
                        .param("node", nodeToLink.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToLink);
    }

    @Test
    void startAlignmentSegmentation_Return404_WhenRequiredNodesNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/alignment", patientId, caseId))
                .andExpect(status().isNotFound());
    }

    @Test
    void startResultPlanning_ShouldAcceptTask_WhenRequiredNodesExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/planning/result", patientId, caseId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void startResultPlanning_Return404_WhenRequiredNodesNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/planning/result", patientId, caseId))
                .andExpect(status().isNotFound());
    }

    @Test
    void startTreatmentPlanning_ShouldAcceptTask_WhenRequiredNodesExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        createResultPlanningNode(patientId, caseId, treatmentCaseService, nodeService);

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/planning/treatment", patientId, caseId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void startTreatmentPlanning_ShouldReturn404_WhenRequiredNodesNotExists() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();


        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/planning/treatment", patientId, caseId))
                .andExpect(status().isNotFound());
    }

    @Test
    void prepareForAlignment_ShouldAcceptTasks_WhenFilesValid() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        MockMultipartFile jawLower = SegmentationTestData.getStlFile("jawLowerStl");
        MockMultipartFile jawUpper = SegmentationTestData.getStlFile("jawUpperStl");
        MockMultipartFile ctArchive = SegmentationTestData.getDicomArchive();

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/prepare", patientId, caseId)
                        .file(jawLower)
                        .file(jawUpper)
                        .file(ctArchive))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
