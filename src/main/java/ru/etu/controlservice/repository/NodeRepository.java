package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.etu.controlservice.entity.Node;

import java.util.Optional;
import java.util.UUID;

public interface NodeRepository extends JpaRepository<Node, UUID> {

    @Query("SELECT n FROM Node n LEFT JOIN FETCH n.ctSegmentation WHERE n.id = :id")
    Optional<Node> findByIdWithCtSegmentation(@Param("id") UUID id);

    @Query("SELECT n FROM Node n LEFT JOIN FETCH n.jawSegmentation WHERE n.id = :id")
    Optional<Node> findByIdWithJawSegmentation(@Param("id") UUID id);

    @Query("SELECT n FROM Node n LEFT JOIN FETCH n.alignmentSegmentation WHERE n.id = :id")
    Optional<Node> findByIdWithAlignmentSegmentation(@Param("id") UUID id);

    @Query("SELECT n FROM Node n LEFT JOIN FETCH n.resultPlanning WHERE n.id = :id")
    Optional<Node> findByIdWithResultPlanning(@Param("id") UUID id);
}
