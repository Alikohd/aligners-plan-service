package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.Node;

public interface NodeRepository extends JpaRepository<Node, Long> {
}
