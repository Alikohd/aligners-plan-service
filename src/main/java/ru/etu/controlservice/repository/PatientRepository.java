package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
