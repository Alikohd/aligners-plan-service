package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.CtSegmentation;

public interface CtSegRepository extends JpaRepository<CtSegmentation, Long> {
}
