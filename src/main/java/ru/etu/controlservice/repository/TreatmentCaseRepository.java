package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.TreatmentCase;

import java.util.Optional;

public interface TreatmentCaseRepository extends JpaRepository<TreatmentCase, Long> {
    @EntityGraph("case-with-nextnodes")
    Optional<TreatmentCase> findByIdAndPatientId(Long id, Long patientId);
}
