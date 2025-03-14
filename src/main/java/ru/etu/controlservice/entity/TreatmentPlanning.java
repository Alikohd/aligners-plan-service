package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class TreatmentPlanning extends BaseTreatmentStep {
    @ManyToOne
    @JoinColumn(name = "result_planning_id", nullable = false)
    private ResultPlanning resultPlanning;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "collections_of_matrices_groups")
    private String collectionsOfMatricesGroups;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachments")
    private List<String> attachments;

}
