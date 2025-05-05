package ru.etu.controlservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.etu.controlservice.dto.nodecontent.AlignmentSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.CtSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.JawSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.ResultPlanningDto;
import ru.etu.controlservice.dto.nodecontent.TreatmentPlanningDto;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.ResultPlanning;
import ru.etu.controlservice.entity.TreatmentPlanning;
import ru.etu.controlservice.exceptions.NodeNotFoundException;
import ru.etu.controlservice.mapper.NodeContentMapper;
import ru.etu.controlservice.repository.NodeRepository;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeContentServiceTest {

    @Mock private NodeContentMapper nodeContentMapper;
    @Mock private TreatmentCaseService caseService;
    @Mock private NodeRepository nodeRepository;

    @InjectMocks private NodeContentService nodeContentService;

    private UUID patientId;
    private UUID caseId;
    private UUID nodeId;

    @BeforeEach
    void setup() {
        patientId = UUID.randomUUID();
        caseId = UUID.randomUUID();
        nodeId = UUID.randomUUID();
    }

    @Test
    void getCtNode_Success() {
        CtSegmentation seg = new CtSegmentation();
        Node node = new Node();
        node.setCtSegmentation(seg);
        CtSegmentationDto dto = new CtSegmentationDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        when(nodeRepository.findByIdWithCtSegmentation(nodeId)).thenReturn(Optional.of(node));
        when(nodeContentMapper.toCtSegmentationDto(seg)).thenReturn(dto);

        CtSegmentationDto result = nodeContentService.getCtNode(patientId, caseId, nodeId);
        assertEquals(dto, result);
    }

    @Test
    void getCtNode_NotFound_Throws() {
        when(nodeRepository.findByIdWithCtSegmentation(nodeId)).thenReturn(Optional.empty());

        assertThrows(NodeNotFoundException.class, () ->
                nodeContentService.getCtNode(patientId, caseId, nodeId));
    }

    @Test
    void getCtNode_NullSegmentation_Throws() {
        Node node = new Node();
        node.setCtSegmentation(null);
        when(nodeRepository.findByIdWithCtSegmentation(nodeId)).thenReturn(Optional.of(node));

        assertThrows(IllegalArgumentException.class, () ->
                nodeContentService.getCtNode(patientId, caseId, nodeId));
    }

    @Test
    void getJawNode_Success() {
        JawSegmentation seg = new JawSegmentation();
        Node node = new Node();
        node.setJawSegmentation(seg);
        JawSegmentationDto dto = new JawSegmentationDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Collections.emptyList());

        when(nodeRepository.findByIdWithJawSegmentation(nodeId)).thenReturn(Optional.of(node));
        when(nodeContentMapper.toJawSegmentationDto(seg)).thenReturn(dto);

        JawSegmentationDto result = nodeContentService.getJawNode(patientId, caseId, nodeId);
        assertEquals(dto, result);
    }

    @Test
    void getAlignmentNode_Success() {
        AlignmentSegmentation seg = new AlignmentSegmentation();
        Node node = new Node();
        node.setAlignmentSegmentation(seg);
        AlignmentSegmentationDto dto = new AlignmentSegmentationDto(UUID.randomUUID(), Collections.emptyList(), Collections.emptyList());

        when(nodeRepository.findByIdWithAlignmentSegmentation(nodeId)).thenReturn(Optional.of(node));
        when(nodeContentMapper.toAlignmentSegmentationDto(seg)).thenReturn(dto);

        AlignmentSegmentationDto result = nodeContentService.getAlignmentNode(patientId, caseId, nodeId);
        assertEquals(dto, result);
    }

    @Test
    void getResultPlanning_Success() {
        ResultPlanning seg = new ResultPlanning();
        Node node = new Node();
        node.setResultPlanning(seg);
        ResultPlanningDto dto = new ResultPlanningDto(UUID.randomUUID(), Collections.emptyList());

        when(nodeRepository.findByIdWithResultPlanning(nodeId)).thenReturn(Optional.of(node));
        when(nodeContentMapper.toResultPlanningDto(seg)).thenReturn(dto);

        ResultPlanningDto result = nodeContentService.getResultPlanning(patientId, caseId, nodeId);
        assertEquals(dto, result);
    }

    @Test
    void getTreatmentPlanning_Success() {
        TreatmentPlanning seg = new TreatmentPlanning();
        Node node = new Node();
        node.setTreatmentPlanning(seg);
        TreatmentPlanningDto dto = new TreatmentPlanningDto(UUID.randomUUID(), Collections.emptyList(), Collections.emptyList());

        when(nodeRepository.findByIdWithTreatmentPlanning(nodeId)).thenReturn(Optional.of(node));
        when(nodeContentMapper.toTreatmentPlanningDto(seg)).thenReturn(dto);

        TreatmentPlanningDto result = nodeContentService.getTreatmentPlanning(patientId, caseId, nodeId);
        assertEquals(dto, result);
    }
}
