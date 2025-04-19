package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.NodeDto;
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
    public NodeDto startCtSegmentation(@PathVariable UUID patientId, @PathVariable UUID caseId,
                                       @RequestParam("ctArchive") MultipartFile ctArchive) {
        return segmentationService.startCtSegmentation(patientId, caseId, ctArchive);
    }

    @PostMapping("jaw")
    public NodeDto startJawSegmentation(@PathVariable UUID patientId, @PathVariable UUID caseId,
                                        @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
                                        @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.startJawSegmentation(patientId, caseId,
                jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
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
    public NodeDto startAlignment(@PathVariable UUID patientId, @PathVariable UUID caseId) {
        return segmentationService.startAlignment(patientId, caseId);
    }
}
