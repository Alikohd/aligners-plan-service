package ru.etu.controlservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.etu.controlservice.dto.PatientDto;
import ru.etu.controlservice.service.PatientService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/patients")
@Tag(name = "Patients", description = "API для управления пациентами")
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать пациента", description = "Создаёт нового пациента и возвращает его данные")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пациент успешно создан"),
    })
    public PatientDto addPatient() {
        return patientService.addPatient();
    }
}