package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.nodecontent.AlignmentSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.CtSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.JawSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.ResultPlanningDto;
import ru.etu.controlservice.dto.nodecontent.TreatmentPlanningDto;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.mapper.NodeContentMapper;
import ru.etu.controlservice.repository.NodeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NodeContentService {
    private final NodeContentMapper nodeContentMapper;
    private final TreatmentCaseService caseService;
    private final NodeRepository nodeRepository;

    public CtSegmentationDto getCtNode(UUID patientId, UUID caseId, UUID nodeId) {
        caseService.getCaseById(patientId, caseId);
        Node node = nodeRepository.findByIdWithCtSegmentation(nodeId).orElseThrow(
                () -> new IllegalArgumentException(String.format("Node with id %s not found", nodeId)));
        if (node.getCtSegmentation() == null) {
            throw new IllegalArgumentException("This node is not CtSegmentation");
        }
        return nodeContentMapper.toCtSegmentationDto(node.getCtSegmentation());
    }

    public JawSegmentationDto getJawNode(UUID patientId, UUID caseId, UUID nodeId) {
        caseService.getCaseById(patientId, caseId);
        Node node = nodeRepository.findByIdWithJawSegmentation(nodeId).orElseThrow(
                () -> new IllegalArgumentException(String.format("Node with id %s not found", nodeId)));
        if (node.getJawSegmentation() == null) {
            throw new IllegalArgumentException("This node is not JawSegmentation");
        }
        return nodeContentMapper.toJawSegmentationDto(node.getJawSegmentation());
    }

    public AlignmentSegmentationDto getAlignmentNode(UUID patientId, UUID caseId, UUID nodeId) {
        caseService.getCaseById(patientId, caseId);
        Node node = nodeRepository.findByIdWithAlignmentSegmentation(nodeId).orElseThrow(
                () -> new IllegalArgumentException(String.format("Node with id %s not found", nodeId)));
        if (node.getAlignmentSegmentation() == null) {
            throw new IllegalArgumentException("This node is not Alignment");
        }
        return nodeContentMapper.toAlignmentSegmentationDto(node.getAlignmentSegmentation());
    }

    public ResultPlanningDto getResultPlanning(UUID patientId, UUID caseId, UUID nodeId) {
        caseService.getCaseById(patientId, caseId);
        Node node = nodeRepository.findByIdWithResultPlanning(nodeId).orElseThrow(
                () -> new IllegalArgumentException(String.format("Node with id %s not found", nodeId)));
        if (node.getResultPlanning() == null) {
            throw new IllegalArgumentException("This node is not ResultPlanning");
        }
        return nodeContentMapper.toResultPlanningDto(node.getResultPlanning());
    }


    public TreatmentPlanningDto getTreatmentPlanning(UUID patientId, UUID caseId, UUID nodeId) {
        caseService.getCaseById(patientId, caseId);
        Node node = nodeRepository.findByIdWithTreatmentPlanning(nodeId).orElseThrow(
                () -> new IllegalArgumentException(String.format("Node with id %s not found", nodeId)));
        if (node.getTreatmentPlanning() == null) {
            throw new IllegalArgumentException("This node is not TreatmentPlanning");
        }
        return nodeContentMapper.toTreatmentPlanningDto(node.getTreatmentPlanning());
    }
}
