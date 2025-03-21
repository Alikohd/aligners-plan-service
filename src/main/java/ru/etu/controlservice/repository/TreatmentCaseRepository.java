package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.TreatmentCase;

import java.util.UUID;

public interface TreatmentCaseRepository extends JpaRepository<TreatmentCase, UUID> {
}
