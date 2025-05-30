package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.JawSegmentation;

import java.util.UUID;

public interface JawSegRepository extends JpaRepository<JawSegmentation, UUID> {
}
