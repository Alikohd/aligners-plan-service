package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.service.PacsService;
import ru.etu.controlservice.service.SegmentationService;
import ru.etu.controlservice.service.TreatmentCaseService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("patients/{patientId}/cases/{caseId}/segmentation")
public class SegmentationController {
    private final PacsService pacsService;
    private final TreatmentCaseService caseService;
    private final SegmentationService segmentationService;


//    @PostMapping(value ="ct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String startCtSegmentation(@PathVariable Long patientId, @PathVariable Long caseId,
//                                      @RequestParam("ctArchive") MultipartFile ctArchive) {
//        List<DicomResponse> dicomResponses = pacsService.sendInstance(ctArchive);
//    }

    @PostMapping("ct")
    public NodeDto startCtSegmentation(@PathVariable Long patientId, @PathVariable Long caseId,
                                       @RequestParam("ctArchive") MultipartFile ctArchive
//                                      @RequestParam("jawUpperStl") MultipartFile jawUpperStl,
//                                      @RequestParam("jawLowerStl") MultipartFile jawLowerStl
    ) {
        return segmentationService.startCtSegmentation(patientId, caseId, ctArchive);
    }

    @PostMapping("jaw")
    public NodeDto startJawSegmentation(@PathVariable Long patientId, @PathVariable Long caseId,
                                        @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
                                        @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.startJawSegmentation(patientId, caseId,
                jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
    }

    @PostMapping("alignment")
    public NodeDto startAlignment(@PathVariable Long patientId, @PathVariable Long caseId) {
        return segmentationService.startAlignment(patientId, caseId);
    }

}
