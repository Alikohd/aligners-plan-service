package ru.etu.controlservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.etu.controlservice.entity.Node;

public interface NodeRepository extends JpaRepository<Node, Long> {

    @Query("SELECT n FROM Node n LEFT JOIN FETCH n.ctSegmentation WHERE n.id = :id")
    Node findByIdWithCtSegmentation(@Param("id") Long id);

    @Query("SELECT n FROM Node n LEFT JOIN FETCH n.jawSegmentation WHERE n.id = :id")
    Node findByIdWithJawSegmentation(@Param("id") Long id);

    @Query("SELECT n FROM Node n LEFT JOIN FETCH n.alignmentSegmentation WHERE n.id = :id")
    Node findByIdWithAlignmentSegmentation(@Param("id") Long id);
}
