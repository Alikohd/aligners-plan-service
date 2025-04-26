package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.MetaNodeDto;
import ru.etu.controlservice.dto.ResultPlanningAmendRequestDto;
import ru.etu.controlservice.dto.TreatmentPlanningAmendRequest;
import ru.etu.controlservice.service.ResultPlanningService;
import ru.etu.controlservice.service.TreatmentPlanningService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("patients/{patientId}/cases/{caseId}/planning")
public class PlanningController {
    private final ResultPlanningService resultPlanningService;
    private final TreatmentPlanningService treatmentPlanningService;

    @PostMapping("result")
    public MetaNodeDto startResultPlanning(@PathVariable UUID patientId, @PathVariable UUID caseId) {
        return resultPlanningService.startResultPlanning(patientId, caseId);
    }

    @PostMapping("result-adjust")
    public MetaNodeDto adjustResultPlanning(@PathVariable UUID patientId,
                                            @PathVariable UUID caseId, @RequestParam("node") UUID nodeId) {
        return resultPlanningService.adjustResult(patientId, caseId, nodeId);
    }

    @PostMapping("result-adjust-inline")
    public MetaNodeDto adjustResultPlanningInline(@PathVariable UUID patientId,
                                                  @PathVariable UUID caseId,
                                                  @RequestBody ResultPlanningAmendRequestDto request) {
        return resultPlanningService.adjustResultInline(patientId, caseId, request.node(), request.amendedDesiredTeethMatrices());
    }

    @PostMapping("treatment")
    public MetaNodeDto startTreatmentPlanning(@PathVariable UUID patientId, @PathVariable UUID caseId) {
        return treatmentPlanningService.startTreatmentPlanning(patientId, caseId);
    }

    @PostMapping("treatment-adjust")
    public MetaNodeDto startTreatmentPlanning(@PathVariable UUID patientId, @PathVariable UUID caseId,
                                              @RequestParam("node") UUID nodeId) {
        return treatmentPlanningService.adjustTreatment(patientId, caseId, nodeId);
    }

    @PostMapping("treatment-adjust-inline")
    public MetaNodeDto startTreatmentPlanning(@PathVariable UUID patientId, @PathVariable UUID caseId,
                                              @RequestBody TreatmentPlanningAmendRequest request) {
        return treatmentPlanningService.adjustTreatmentInline(patientId, caseId,
                request.node(), request.treatmentStepMatrixGroup(), request.attachment());
    }
}
