package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.AlignmentSegmentation;

import java.util.UUID;

public interface AlignmentSegRepository extends JpaRepository<AlignmentSegmentation, UUID> {
}
