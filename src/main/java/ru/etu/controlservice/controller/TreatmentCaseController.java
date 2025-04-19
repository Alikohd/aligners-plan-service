package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.TreatmentCaseDto;
import ru.etu.controlservice.service.PatientService;
import ru.etu.controlservice.service.TreatmentCaseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("patients/{patientId}/cases")
public class TreatmentCaseController {
    private final TreatmentCaseService caseService;
    private final PatientService patientService;

    @PostMapping
    public TreatmentCaseDto addCase(@PathVariable UUID patientId) {
        return caseService.createCase(patientId);
    }

    @GetMapping
    public List<TreatmentCaseDto> getCases(@PathVariable UUID patientId) {
        return caseService.getAllCases(patientId);
    }

    @GetMapping("/{caseId}")
    public TreatmentCaseDto getCase(@PathVariable UUID patientId, @PathVariable UUID caseId) {
        return caseService.getCaseDtoById(patientId, caseId);
    }

}
