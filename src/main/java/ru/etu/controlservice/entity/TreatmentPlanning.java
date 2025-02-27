package ru.etu.controlservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@Entity
public class TreatmentPlanning {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "treatment_planning_id", nullable = false)
    private TreatmentPlanning treatmentPlanning;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<List<List<String>>> collectionsOfMatricesGroups;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> attachments;
}
