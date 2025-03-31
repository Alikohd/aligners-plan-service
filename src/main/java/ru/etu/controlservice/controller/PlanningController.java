package ru.etu.controlservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.service.PlanningService;

@RestController
@RequiredArgsConstructor
@RequestMapping("patients/{patientId}/cases/{caseId}/planning")
public class PlanningController {
    private final PlanningService planningService;

    @PostMapping("result")
    public NodeDto startResultPlanning(@PathVariable Long patientId, @PathVariable Long caseId) {
        return planningService.startResultPlanning(patientId, caseId);
    }

}
