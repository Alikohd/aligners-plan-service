package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.TreatmentCase;

public interface TreatmentCaseRepository extends JpaRepository<TreatmentCase, Long> {
}
