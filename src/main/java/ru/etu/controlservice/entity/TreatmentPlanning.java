package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "treatment_planning")
public class TreatmentPlanning {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "treatment_planning_id", nullable = false)
    private TreatmentPlanning treatmentPlanning;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "collections_of_matrices_groups")
    private List<List<List<String>>> collectionsOfMatricesGroups;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments")
    private List<String> attachments;

    @OneToOne
    @JoinColumn(name = "node_id", unique = true)
    private Node node;
}
