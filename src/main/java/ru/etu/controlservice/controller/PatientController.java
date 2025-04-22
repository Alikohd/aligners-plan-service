package ru.etu.controlservice.controller;

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
@RequestMapping("patients")
public class PatientController {
    private final PatientService patientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto addPatient() {
        return patientService.addPatient();
    }
}
