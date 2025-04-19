package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.TreatmentCaseDto;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.Patient;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.CaseNotFoundException;
import ru.etu.controlservice.exceptions.PatientNotFoundException;
import ru.etu.controlservice.mapper.TreatmentCaseMapper;
import ru.etu.controlservice.repository.PatientRepository;
import ru.etu.controlservice.repository.TreatmentCaseRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TreatmentCaseService {
    private final PatientRepository patientRepository;
    private final TreatmentCaseRepository caseRepository;
    private final TreatmentCaseMapper caseMapper;

    public TreatmentCaseDto createCase(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(String.format("Patient with id %d not found", patientId)));

        Node root = new Node();

        TreatmentCase treatmentCase = new TreatmentCase();
        treatmentCase.setRoot(root);
        patient.addCase(treatmentCase);

        return caseMapper.entityToDto(caseRepository.save(treatmentCase));
    }

    public List<TreatmentCaseDto> getAllCases(UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(String.format("Patient with id %d not found", patientId)));

        return patient.getCases().stream().map(caseMapper::entityToDto).toList();
    }

    public TreatmentCase getCaseById(UUID patientId, UUID caseId) {
        patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(String.format("Patient with id %d not found", patientId)));

        return caseRepository.findByIdAndPatientId(caseId, patientId)
                .orElseThrow(() -> new CaseNotFoundException(String.format("Case with id %d not found", caseId)));
    }

    public TreatmentCaseDto getCaseDtoById(UUID patientId, UUID caseId) {
        TreatmentCase treatmentCase = getCaseById(patientId, caseId);
        return caseMapper.entityToDto(treatmentCase);
    }
}
