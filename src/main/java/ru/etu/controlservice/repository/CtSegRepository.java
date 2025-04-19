package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.CtSegmentation;

import java.util.UUID;

public interface CtSegRepository extends JpaRepository<CtSegmentation, UUID> {
}
