package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.nodecontent.AlignmentSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.CtSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.JawSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.ResultPlanningDto;
import ru.etu.controlservice.dto.nodecontent.TreatmentPlanningDto;
import ru.etu.controlservice.service.NodeContentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patients/{patientId}/cases/{caseId}")
public class NodeContentController {
    private final NodeContentService nodeContentService;

    @GetMapping("/segmentation/ct/{nodeId}")
    public CtSegmentationDto getSegmentationCtNode(@PathVariable UUID patientId, @PathVariable UUID caseId, @PathVariable UUID nodeId) {
        return nodeContentService.getCtNode(patientId, caseId, nodeId);
    }

    @GetMapping("/segmentation/jaw/{nodeId}")
    public JawSegmentationDto getSegmentationJawNode(@PathVariable UUID patientId, @PathVariable UUID caseId, @PathVariable UUID nodeId) {
        return nodeContentService.getJawNode(patientId, caseId, nodeId);
    }

    @GetMapping("/segmentation/alignment/{nodeId}")
    public AlignmentSegmentationDto getSegmentationAlignmentNode(@PathVariable UUID patientId, @PathVariable UUID caseId, @PathVariable UUID nodeId) {
        return nodeContentService.getAlignmentNode(patientId, caseId, nodeId);
    }

    @GetMapping("/planning/result/{nodeId}")
    public ResultPlanningDto getResultPlanningNode(@PathVariable UUID patientId, @PathVariable UUID caseId, @PathVariable UUID nodeId) {
        return nodeContentService.getResultPlanning(patientId, caseId, nodeId);
    }

    @GetMapping("/planning/treatment/{nodeId}")
    public TreatmentPlanningDto getTreatmentPlanningNode(@PathVariable UUID patientId, @PathVariable UUID caseId, @PathVariable UUID nodeId) {
        return nodeContentService.getTreatmentPlanning(patientId, caseId, nodeId);
    }
}
