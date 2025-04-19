package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.TreatmentCase;

import java.util.Optional;
import java.util.UUID;

public interface TreatmentCaseRepository extends JpaRepository<TreatmentCase, UUID> {
    @EntityGraph("case-with-nextnodes")
    Optional<TreatmentCase> findByIdAndPatientId(UUID id, UUID patientId);
}
