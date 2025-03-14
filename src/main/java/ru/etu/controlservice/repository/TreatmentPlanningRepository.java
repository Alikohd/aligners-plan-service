package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.TreatmentPlanning;

public interface TreatmentPlanningRepository extends JpaRepository<TreatmentPlanning, Long> {
}
