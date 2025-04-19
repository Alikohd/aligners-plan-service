package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.TreatmentPlanning;

import java.util.UUID;

public interface TreatmentPlanningRepository extends JpaRepository<TreatmentPlanning, UUID> {
}
