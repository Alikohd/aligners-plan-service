package ru.etu.controlservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.uuid.Generators;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "treatment_planning")
public class TreatmentPlanning {
    @Id
    private UUID id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "treatment_step_matrix_group")
    private JsonNode treatmentStepMatrixGroup;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attachment")
    private JsonNode attachment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = Generators.timeBasedEpochGenerator().generate(); // UUID v7 generation
        }
    }
}
