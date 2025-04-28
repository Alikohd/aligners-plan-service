package ru.etu.controlservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.etu.controlservice.dto.MetaNodeDto;
import ru.etu.controlservice.dto.ResultPlanningAmendRequestDto;
import ru.etu.controlservice.dto.TreatmentPlanningAmendRequest;
import ru.etu.controlservice.service.ResultPlanningService;
import ru.etu.controlservice.service.TreatmentPlanningService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patients/{patientId}/cases/{caseId}/planning")
@Tag(name = "Planning", description = "API для обработки этапов планирования лечения пациента")
public class PlanningController {
    private final ResultPlanningService resultPlanningService;
    private final TreatmentPlanningService treatmentPlanningService;

    @PostMapping("/result")
    @Operation(summary = "Принять задачу планирования результата", description = "Добавляет в очередь обработки задачу по планированию результата лечения для указанного пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача планирования результата принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public MetaNodeDto startResultPlanning(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId) {
        return resultPlanningService.startResultPlanning(patientId, caseId);
    }

    @PostMapping("/result-adjust")
    @Operation(summary = "Принять задачу корректировки планирования результата", description = "Добавляет в очередь обработки задачу по корректировке планирования результата с альтернативным подходом. Создаёт новый узел планирования как альтернативу указанному, не удаляя его")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача корректировки планирования результата принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверный nodeId"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustResultPlanning(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId) {
        return resultPlanningService.adjustResult(patientId, caseId, nodeId);
    }

    @PostMapping("/result-adjust-inline")
    @Operation(summary = "Править матрицы планирования результата", description = "Заменяет матрицы желаемого положения зубов на заданные для указанного узла, пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Матрицы планирования результата заменены", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustResultPlanningInline(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId,
            @Parameter(description = "Матрицы нового желаемого положения зубов") @RequestBody ResultPlanningAmendRequestDto request) {
        return resultPlanningService.adjustResultInline(patientId, caseId, nodeId, request.amendedDesiredTeethMatrices());
    }

    @PostMapping("/treatment")
    @Operation(summary = "Принять задачу планирования лечения", description = "Добавляет в очередь обработки задачу по планированию лечения для указанного пациента и случая. В результате обработки задачи создается последовательность узлов соответствующая шагам лечения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача планирования лечения принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public MetaNodeDto startTreatmentPlanning(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId) {
        return treatmentPlanningService.startTreatmentPlanning(patientId, caseId);
    }

    @PostMapping("/treatment-adjust")
    @Operation(summary = "Принять задачу корректировки планирования лечения", description = "Добавляет в очередь обработки задачу по корректировке планирования лечения с альтернативным подходом. В качестве альтернативы указанному узлу создается последовательность новых узлов соответствующих шагам лечениям")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача корректировки планирования лечения принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверный nodeId"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustTreatment(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId) {
        return treatmentPlanningService.adjustTreatment(patientId, caseId, nodeId);
    }

    @PostMapping("/treatment-adjust-inline")
    @Operation(summary = "Править шаг лечения", description = "Заменяет шаг лечения на заданный для указанного узла, пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "План лечения заменён", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustTreatmentInline(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId,
            @Parameter(description = "Набор матриц положения зубов соответствующих шагу лечения") @RequestBody TreatmentPlanningAmendRequest request) {
        return treatmentPlanningService.adjustTreatmentInline(patientId, caseId,
                nodeId, request.treatmentStepMatrixGroup(), request.attachment());
    }
}