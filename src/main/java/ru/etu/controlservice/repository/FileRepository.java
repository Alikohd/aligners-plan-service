package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.File;

import java.util.UUID;

public interface FileRepository extends JpaRepository<File, UUID> {
}
