package ru.etu.controlservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
@RequestMapping("/patients/{patientId}/cases/{caseId}/segmentation")
public class SegmentationController {
    private final SegmentationService segmentationService;

    @PostMapping(value = "/ct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "CT Segmentation")
    @Operation(summary = "Принять задачу сегментации КТ", description = "Добавляет в очередь обработки задачу по сегментации КТ для указанного пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача сегментации КТ принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат архива КТ"),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public MetaNodeDto startCtSegmentation(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Архив с данными КТ") @RequestParam("ctArchive") MultipartFile ctArchive) {
        return segmentationService.startCtSegmentation(patientId, caseId, ctArchive);
    }

    @PostMapping(value = "/ct-adjust", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "CT Segmentation")
    @Operation(summary = "Принять задачу корректировки сегментации КТ", description = "Добавляет в очередь обработки задачу по корректировке сегментации КТ с альтернативным подходом. Создает новый узел сегментации как альтернативу указанному, не удаляя его")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача корректировки сегментации КТ принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат архива КТ"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustCt(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId,
            @Parameter(description = "Архив с данными нового КТ") @RequestParam("ctArchive") MultipartFile ctArchive) {
        return segmentationService.adjustCt(patientId, caseId, nodeId, ctArchive);
    }

    @PostMapping(value = "/ct-adjust-inline", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "CT Segmentation")
    @Operation(summary = "Править результат сегментации КТ", description = "Заменяет маску КТ на заданную для указанного узла, пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Маска КТ заменена", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат маски КТ"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustCtInline(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId,
            @Parameter(description = "Архив с данными новой маски КТ") @RequestParam("amendedCtMask") MultipartFile amendedCtMask) {
        return segmentationService.adjustCtInline(patientId, caseId, nodeId, amendedCtMask);
    }

    @PostMapping(value = "/jaw", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "Jaw Segmentation")
    @Operation(summary = "Принять задачу сегментации челюстей", description = "Добавляет в очередь обработки задачу по сегментации челюстей для указанного пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача сегментации челюстей принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат STL-файлов"),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public MetaNodeDto startJawSegmentation(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "STL-файл нижней челюсти") @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
            @Parameter(description = "STL-файл верхней челюсти") @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.startJawSegmentation(patientId, caseId, jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
    }

    @PostMapping(value = "/jaw-adjust", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "Jaw Segmentation")
    @Operation(summary = "Принять задачу корректировки сегментации челюстей", description = "Добавляет в очередь обработки задачу по корректировке сегментации челюстей с альтернативным подходом. Создает новый узел сегментации как альтернативу указанному, не удаляя его")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача корректировки сегментации челюстей принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат STL-файлов"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustJaw(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId,
            @Parameter(description = "Новый STL-файл нижней челюсти") @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
            @Parameter(description = "Новый STL-файл верхней челюсти") @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.adjustJaw(patientId, caseId, nodeId, jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
    }

    @PostMapping("/jaw-adjust-inline")
    @Tag(name = "Jaw Segmentation")
    @Operation(summary = "Править результат сегментации челюстей", description = "Заменяет результат сегментации челюстей на заданный для указанного узла, пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат сегментации челюстей заменен", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат результата сегментации челюстей"),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public MetaNodeDto adjustJawInline(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId,
            @Parameter(description = "Новый результат сегментации челюстей") @RequestBody JawAmendRequestDto request) {
        return segmentationService.adjustJawInline(patientId, caseId, nodeId, request.amendedJawsSegmented());
    }

    @PostMapping(value = "/prepare", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Tag(name = "Alignment")
    @Operation(summary = "Принять задачи по подготовке этапов для совмещения", description = "Добавляет в очередь обработки задачи по сегментации КТ и челюстей для указанного пациента и случая, допуская их параллельную обработку")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задачи по подготовке к совмещению приняты", content = @Content(schema = @Schema(implementation = NodePairDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный формат файлов"),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public NodePairDto prepareForAlignment(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Архив с данными КТ") @RequestParam("ctArchive") MultipartFile ctArchive,
            @Parameter(description = "STL-файл нижней челюсти") @RequestParam("jawLowerStl") MultipartFile jawLowerStl,
            @Parameter(description = "STL-файл верхней челюсти") @RequestParam("jawUpperStl") MultipartFile jawUpperStl) throws IOException {
        return segmentationService.prepareForAlignment(patientId, caseId, ctArchive, jawUpperStl.getInputStream(), jawLowerStl.getInputStream());
    }

    @PostMapping("/alignment")
    @Tag(name = "Alignment")
    @Operation(summary = "Принять задачу совмещения", description = "Добавляет в очередь обработки задачу по совмещению сегментаций КТ и челюстей для указанного пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача совмещения принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public MetaNodeDto startAlignment(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId) {
        return segmentationService.startAlignment(patientId, caseId);
    }

    @PostMapping("/alignment-adjust")
    @Tag(name = "Alignment")
    @Operation(summary = "Принять задачу корректировки совмещения", description = "Добавляет в очередь обработки задачу по корректировке совмещения с альтернативным подходом. Создает новый узел совмещения как альтернативу указанному, не удаляя его")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача корректировки совмещения принята", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверный nodeId"),
            @ApiResponse(responseCode = "404", description = "Пациент, случай или узел не найден")
    })
    public MetaNodeDto adjustAlignment(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId) {
        return segmentationService.adjustAlignment(patientId, caseId, nodeId);
    }

    @PostMapping("/alignment-adjust-inline")
    @Tag(name = "Alignment")
    @Operation(summary = "Править результат совмещения", description = "Заменяет результат совмещения на заданный для указанного узла, пациента и случая")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат совмещения заменен", content = @Content(schema = @Schema(implementation = MetaNodeDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Пациент или случай не найден")
    })
    public MetaNodeDto adjustAlignmentInline(
            @Parameter(description = "Идентификатор пациента", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID patientId,
            @Parameter(description = "Идентификатор случая", example = "123e4567-e89b-12d3-a456-426614174001") @PathVariable UUID caseId,
            @Parameter(description = "Идентификатор узла", example = "123e4567-e89b-12d3-a456-426614174002") @RequestParam("node") UUID nodeId,
            @Parameter(description = "Новые матрицы исходного положения зубов") @RequestBody AlignmentAmendRequestDto request) {
        return segmentationService.adjustAlignmentInline(patientId, caseId, nodeId, request.amendedInitTeethMatrices());
    }
}