package ru.etu.controlservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.FileDto;
import ru.etu.controlservice.service.CommonFileService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "File Management")
public class FileController {
    private final CommonFileService commonFileService;

    @GetMapping("/{fileId}")
    @Operation(summary = "Получить файл по идентификатору", description = "Возвращает файл, связанный с указанным идентификатором, для скачивания. Идентификатор может быть получен из содержимого узлов.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Файл успешно получен", content = @Content(mediaType = "application/octet-stream", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Файл не найден")
    })
    public ResponseEntity<Resource> getFile(
            @Parameter(description = "Идентификатор файла", example = "123e4567-e89b-12d3-a456-426614174000") @PathVariable UUID fileId) throws IOException {
        FileDto file = commonFileService.getFile(fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.content().contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.name() + "\"")
                .body(file.content());
    }
}