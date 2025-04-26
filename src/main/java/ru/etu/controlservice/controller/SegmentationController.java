package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.AlignmentAmendRequestDto;
import ru.etu.controlservice.dto.JawAmendRequestDto;
import ru.etu.controlservice.dto.MetaNodeDto;
import ru.etu.controlservice.dto.NodePairDto;
import ru.etu.controlservice.service.SegmentationService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("patients/{patientId}/cases/{caseId}/segmentation")
public class SegmentationController {
    private final SegmentationService segmentationService;

    @PostMapping("ct")
    public MetaNodeDto startCtSegmentation(@PathVariable UUID patientId, @PathVariable UUID caseId,
                                           @RequestParam("ctArchive") MultipartFile ctArchive) {
        return segmentationService.startCtSegmentation(patientId, caseId, ctArchive);
    }

    @PostMapping("/ct-adjust")
    public MetaNodeDto adjustCt(@PathVariable UUID caseId,
                                @PathVariable UUID patientId,
                                @RequestParam("node") UUID nodeId,
                                @RequestParam("ctArchive") MultipartFile ctArchive) {
        return segmentationService.adjustCt(patientId, caseId, nodeId, ctArchive);
    }

    @PostMapping("/ct-adjust-inline")
    public MetaNodeDto adjustCtInline(@PathVariable UUID caseId,
                                      @PathVariable UUID patientId,
                                      @RequestParam("node") UUID nodeId,
                                      @RequestParam("amendedCtMask") MultipartFile amendedCtMask) {
        return segmentationService.adjustCtInline(patientId, caseId, nodeId, amendedCtMask);
    }

    @PostMapping("jaw")
    public MetaNodeDto startJawSegmentation(@PathVariable UUID patientId, @PathVariable UUID caseId,
                                            @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
                                            @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.startJawSegmentation(patientId, caseId,
                jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
    }

    @PostMapping("jaw-adjust")
    public MetaNodeDto adjustJaw(@PathVariable UUID patientId,
                                 @PathVariable UUID caseId,
                                 @RequestParam("node") UUID nodeId,
                                 @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
                                 @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.adjustJaw(patientId, caseId, nodeId, jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
    }

    @PostMapping("jaw-adjust-inline")
    public MetaNodeDto adjustJawInline(@PathVariable UUID patientId,
                                       @PathVariable UUID caseId,
                                       @RequestBody JawAmendRequestDto request) {
        return segmentationService.adjustJawInline(patientId, caseId, request.node(), request.amendedJawsSegmented());
    }

    @PostMapping("prepare")
    public NodePairDto prepareForAlignment(@PathVariable UUID patientId, @PathVariable UUID caseId,
                                           @RequestParam("ctArchive") MultipartFile ctArchive,
                                           @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
                                           @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.prepareForAlignment(patientId, caseId, ctArchive,
                jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
    }

    @PostMapping("alignment")
    public MetaNodeDto startAlignment(@PathVariable UUID patientId, @PathVariable UUID caseId) {
        return segmentationService.startAlignment(patientId, caseId);
    }

    @PostMapping("alignment-adjust")
    public MetaNodeDto adjustAlignment(@PathVariable UUID patientId,
                                       @PathVariable UUID caseId,
                                       @RequestParam("node") UUID nodeId) {
        return segmentationService.adjustAlignment(patientId, caseId, nodeId);
    }

    @PostMapping("alignment-adjust-inline")
    public MetaNodeDto adjustAlignmentInline(@PathVariable UUID patientId,
                                             @PathVariable UUID caseId,
                                             @RequestBody AlignmentAmendRequestDto request) {
        return segmentationService.adjustAlignmentInline(patientId, caseId, request.node(), request.amendedInitTeethMatrices());
    }
}
