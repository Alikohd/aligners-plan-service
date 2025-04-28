package ru.etu.controlservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.FlatNodeDto;
import ru.etu.controlservice.dto.TreatmentCaseDto;
import ru.etu.controlservice.service.NodeGraphService;
import ru.etu.controlservice.service.TreatmentCaseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patients/{patientId}/cases")
@Tag(name = "Treatment Cases", description = "API для управления случаями лечения пациентов")
public class TreatmentCaseController {
    private final TreatmentCaseService caseService;
    private final NodeGraphService nodeGraphService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать случай лечения", description = "Создает случай лечения для указанного пациента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Случай лечения создан", content = @Content(schema = @Schema(implementation = TreatmentCaseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный идентификатор пациента"),
            @ApiResponse(responseCode = "404", description = "Пациент не найден")
    })
    public TreatmentCaseDto addCase(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId) {
        return caseService.createCase(patientId);
    }

    @GetMapping
    @Operation(summary = "Получить все случаи лечения", description = "Возвращает список всех случаев лечения для указанного пациента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список случаев лечения получен", content = @Content(schema = @Schema(implementation = TreatmentCaseDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент не найден")
    })
    public List<TreatmentCaseDto> getCases(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId) {
        return caseService.getAllCases(patientId);
    }

    @GetMapping("/{caseId}")
    @Operation(summary = "Получить случай лечения", description = "Возвращает данные конкретного случая лечения для указанного пациента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные случая лечения получены", content = @Content(schema = @Schema(implementation = TreatmentCaseDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public TreatmentCaseDto getCase(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId) {
        return caseService.getCaseDtoById(patientId, caseId);
    }

    @GetMapping("/{caseId}/treatment-plan")
    @Operation(summary = "Получить план лечения", description = "Возвращает граф, представляющий план лечения в виде плоской структуры со связями узлов для указанного случая и пациента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "План лечения получен", content = @Content(schema = @Schema(implementation = FlatNodeDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public List<FlatNodeDto> getTreatmentPlan(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId) {
        return nodeGraphService.getFlatGraph(patientId, caseId);
    }
}
