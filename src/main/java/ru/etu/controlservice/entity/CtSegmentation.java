package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "ct_segmentation")
public class CtSegmentation extends BaseTreatmentStep {
    @Column(name = "ct_original")
    private String ctOriginal;

    @Column(name = "ct_mask")
    private String ctMask;

    @Builder
    public CtSegmentation(Long id, String ctOriginal, String ctMask) {
        super(id);
        this.ctOriginal = ctOriginal;
        this.ctMask = ctMask;
    }
}
