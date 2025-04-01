package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.service.ResultPlanningService;
import ru.etu.controlservice.service.TreatmentPlanningService;

@RestController
@RequiredArgsConstructor
@RequestMapping("patients/{patientId}/cases/{caseId}/planning")
public class PlanningController {
    private final ResultPlanningService resultPlanningService;
    private final TreatmentPlanningService treatmentPlanningService;

    @PostMapping("result")
    public NodeDto startResultPlanning(@PathVariable Long patientId, @PathVariable Long caseId) {
        return resultPlanningService.startResultPlanning(patientId, caseId);
    }

    @PostMapping("treatment")
    public NodeDto startTreatmentPlanning(@PathVariable Long patientId, @PathVariable Long caseId) {
        return treatmentPlanningService.startTreatmentPlanning(patientId, caseId);
    }
}
