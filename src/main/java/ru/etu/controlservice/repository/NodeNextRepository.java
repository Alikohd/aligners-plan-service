package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.etu.controlservice.entity.NodeNextRelation;
import ru.etu.controlservice.entity.id.NodeNextRelationId;

public interface NodeNextRepository extends JpaRepository<NodeNextRelation, NodeNextRelationId> {
}
