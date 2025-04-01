package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "result_planning")
public class ResultPlanning extends BaseTreatmentStep {
    @ManyToOne
    @JoinColumn(name = "alignment_segmentation_id", nullable = false)
    private AlignmentSegmentation alignmentSegmentation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "desired_teeth_matrices")
    private List<String> desiredTeethMatrices = new ArrayList<>();

}
