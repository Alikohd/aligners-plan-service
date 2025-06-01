package ru.etu.controlservice.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.etu.controlservice.dto.AlignmentAmendRequestDto;
import ru.etu.controlservice.dto.JawAmendRequestDto;
import ru.etu.controlservice.dto.ResultPlanningAmendRequestDto;
import ru.etu.controlservice.dto.TreatmentPlanningAmendRequest;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.integration.TestContainersConfig;
import ru.etu.controlservice.service.NodeService;
import ru.etu.controlservice.service.PatientService;
import ru.etu.controlservice.service.TreatmentCaseService;
import ru.etu.controlservice.util.SegmentationTestData;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.etu.controlservice.util.NodeTestUtils.*;

@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdjustModelingIT extends TestContainersConfig {
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
    void adjustCt_ShouldAcceptTaskAndBranching() throws Exception {
        MockMultipartFile ctArchive = SegmentationTestData.getDicomArchive();
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeToBranch = nodeService.addStepToEnd(treatmentCase);

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/ct-adjust", patientId, caseId)
                        .file(ctArchive)
                        .param("node", nodeToBranch.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToBranch.getPrevNode().getId());
    }

    @Test
    void adjustJaw_ShouldAcceptTaskAndBranching() throws Exception {
        MockMultipartFile jawLower = SegmentationTestData.getStlFile("jawLowerStl");
        MockMultipartFile jawUpper = SegmentationTestData.getStlFile("jawUpperStl");
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        Node nodeToBranch = nodeService.addStepToEnd(treatmentCase);

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/jaw-adjust", patientId, caseId)
                        .file(jawLower)
                        .file(jawUpper)
                        .param("node", nodeToBranch.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToBranch.getPrevNode().getId());
    }

    @Test
    void adjustAlignment_ShouldAcceptTaskAndBranching() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        Node nodeToBranch = nodeService.addStepToEnd(treatmentCase);

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/alignment-adjust", patientId, caseId)
                        .param("node", nodeToBranch.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToBranch.getPrevNode().getId());
    }

    @Test
    void adjustResult_ShouldAcceptTaskAndBranching() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        Node nodeToBranch = nodeService.addStepToEnd(treatmentCase);

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/planning/result-adjust", patientId, caseId)
                        .param("node", nodeToBranch.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToBranch.getPrevNode().getId());
    }

    @Test
    void adjustTreatment_ShouldAcceptTaskAndBranching() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createResultPlanningNode(patientId, caseId, treatmentCaseService, nodeService);
        Node nodeToBranch = nodeService.addStepToEnd(treatmentCase);

        MvcResult result = mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/planning/treatment-adjust", patientId, caseId)
                        .param("node", nodeToBranch.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        UUID returnedNodeId = UUID.fromString(JsonPath.read(responseJson, "$.id"));
        UUID prevNodeId = nodeService.getNode(returnedNodeId).getPrevNode().getId();
        assertEquals(prevNodeId, nodeToBranch.getPrevNode().getId());
    }

    @Test
    void adjustCtInline_ShouldUpdateCtNode() throws Exception {
        MockMultipartFile ctArchive = SegmentationTestData.getDicomArchiveAmended();
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        Node ctNode = nodeService.findLastNode(treatmentCase.getRoot());
        String oldCtMaskUri = ctNode.getCtSegmentation().getCtMask().getUri();

        mockMvc.perform(multipart("/patients/{patientId}/cases/{caseId}/segmentation/ct-adjust-inline", patientId, caseId)
                        .file(ctArchive)
                        .param("node", ctNode.getId().toString())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String newCtMaskUri = ctNode.getCtSegmentation().getCtMask().getUri();
        assertNotEquals(oldCtMaskUri, newCtMaskUri);
    }

    @Test
    void adjustJawInline_ShouldUpdateJawNode() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        Node jawNode = nodeService.findLastNode(treatmentCase.getRoot());
        List<JsonNode> oldJawsSegmented = jawNode.getJawSegmentation().getJawsSegmented();

        JawAmendRequestDto request = new JawAmendRequestDto(List.of(SegmentationTestData.getMockJson()));
        mockMvc.perform(put("/patients/{patientId}/cases/{caseId}/segmentation/jaw-adjust-inline", patientId, caseId)
                        .param("node", jawNode.getId().toString())
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<JsonNode> newJawsSegmented = jawNode.getJawSegmentation().getJawsSegmented();
        assertNotEquals(oldJawsSegmented, newJawsSegmented);
    }

    @Test
    void adjustAlignmentInline_ShouldUpdateAlignmentNode() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        Node alignmentNode = nodeService.findLastNode(treatmentCase.getRoot());
        List<JsonNode> oldInitTeethMatrices = alignmentNode.getAlignmentSegmentation().getInitTeethMatrices();
        AlignmentAmendRequestDto request = new AlignmentAmendRequestDto(List.of(SegmentationTestData.getUpdatedMockJson()));

        mockMvc.perform(put("/patients/{patientId}/cases/{caseId}/segmentation/alignment-adjust-inline", patientId, caseId)
                        .param("node", alignmentNode.getId().toString())
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<JsonNode> newInitTeethMatrices = alignmentNode.getAlignmentSegmentation().getInitTeethMatrices();
        assertNotEquals(oldInitTeethMatrices, newInitTeethMatrices);
    }

    @Test
    void adjustResultInline_ShouldUpdateResultNode() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createResultPlanningNode(patientId, caseId, treatmentCaseService, nodeService);
        Node resultNode = nodeService.findLastNode(treatmentCase.getRoot());
        List<JsonNode> oldDesiredTeethMatrices = resultNode.getResultPlanning().getDesiredTeethMatrices();
        ResultPlanningAmendRequestDto request = new ResultPlanningAmendRequestDto(List.of(SegmentationTestData.getUpdatedMockJson()));

        mockMvc.perform(put("/patients/{patientId}/cases/{caseId}/planning/result-adjust-inline", patientId, caseId)
                        .param("node", resultNode.getId().toString())
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        List<JsonNode> newInitTeethMatrices = resultNode.getResultPlanning().getDesiredTeethMatrices();
        assertNotEquals(oldDesiredTeethMatrices, newInitTeethMatrices);
    }

    @Test
    void adjustTreatmentInline_ShouldUpdateTreatmentNode() throws Exception {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        createTreatmentStepNode(patientId, caseId, treatmentCaseService, nodeService);
        Node treatmentNode = nodeService.findLastNode(treatmentCase.getRoot());
        JsonNode oldMatrixGroup = treatmentNode.getTreatmentPlanning().getTreatmentStepMatrixGroup();
        JsonNode oldAttachment = treatmentNode.getTreatmentPlanning().getAttachment();
        TreatmentPlanningAmendRequest request = new TreatmentPlanningAmendRequest(SegmentationTestData.getUpdatedMockJson(), SegmentationTestData.getUpdatedMockJson());

        mockMvc.perform(put("/patients/{patientId}/cases/{caseId}/planning/treatment-adjust-inline", patientId, caseId)
                        .param("node", treatmentNode.getId().toString())
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode newMatrixGroup = treatmentNode.getTreatmentPlanning().getTreatmentStepMatrixGroup();
        JsonNode newAttachment = treatmentNode.getTreatmentPlanning().getAttachment();

        assertNotEquals(oldMatrixGroup, newMatrixGroup);
        assertNotEquals(oldAttachment, newAttachment);
    }


}
