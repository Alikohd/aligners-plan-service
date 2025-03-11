package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.ResultPlanning;

public interface ResultPlanningRepository extends JpaRepository<ResultPlanning, Long> {
}
