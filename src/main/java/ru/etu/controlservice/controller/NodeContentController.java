package ru.etu.controlservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Node Content")
public class NodeContentController {
    private final NodeContentService nodeContentService;

    @GetMapping("/segmentation/ct/{nodeId}")
    @Operation(summary = "Получить данные узла сегментации КТ", description = "Возвращает данные сегментации КТ для указанного пациента, случая и узла")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные узла сегментации КТ успешно получены", content = @Content(schema = @Schema(implementation = CtSegmentationDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public CtSegmentationDto getSegmentationCtNode(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @PathVariable UUID nodeId) {
        return nodeContentService.getCtNode(patientId, caseId, nodeId);
    }

    @GetMapping("/segmentation/jaw/{nodeId}")
    @Operation(summary = "Получить данные узла сегментации челюстей", description = "Возвращает данные сегментации челюстей для указанного пациента, случая и узла")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные узла сегментации челюстей успешно получены", content = @Content(schema = @Schema(implementation = JawSegmentationDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public JawSegmentationDto getSegmentationJawNode(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @PathVariable UUID nodeId) {
        return nodeContentService.getJawNode(patientId, caseId, nodeId);
    }

    @GetMapping("/segmentation/alignment/{nodeId}")
    @Operation(summary = "Получить данные узла совмещения", description = "Возвращает данные совмещения сегментаций КТ и челюстей для указанного пациента, случая и узла")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные узла совмещения успешно получены", content = @Content(schema = @Schema(implementation = AlignmentSegmentationDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public AlignmentSegmentationDto getSegmentationAlignmentNode(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @PathVariable UUID nodeId) {
        return nodeContentService.getAlignmentNode(patientId, caseId, nodeId);
    }

    @GetMapping("/planning/result/{nodeId}")
    @Operation(summary = "Получить данные узла результата планирования", description = "Возвращает данные результата планирования лечения для указанного пациента, случая и узла")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные узла результата планирования успешно получены", content = @Content(schema = @Schema(implementation = ResultPlanningDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public ResultPlanningDto getResultPlanningNode(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @PathVariable UUID nodeId) {
        return nodeContentService.getResultPlanning(patientId, caseId, nodeId);
    }

    @GetMapping("/planning/treatment/{nodeId}")
    @Operation(summary = "Получить данные узла плана лечения", description = "Возвращает данные плана лечения для указанного пациента, случая и узла")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные узла плана лечения успешно получены", content = @Content(schema = @Schema(implementation = TreatmentPlanningDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public TreatmentPlanningDto getTreatmentPlanningNode(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @PathVariable UUID nodeId) {
        return nodeContentService.getTreatmentPlanning(patientId, caseId, nodeId);
    }
}