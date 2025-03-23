package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.PatientDto;
import ru.etu.controlservice.entity.Patient;
import ru.etu.controlservice.mapper.PatientMapper;
import ru.etu.controlservice.repository.PatientRepository;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientDto addPatient() {
        Patient savedPatient = patientRepository.save(new Patient());
        return patientMapper.entityToDto(savedPatient);
    }
}
