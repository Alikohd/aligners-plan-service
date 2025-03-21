package ru.etu.controlservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
    public CtSegmentation(Long id, Node node, String ctOriginal, String ctMask) {
        super(id, node);
        this.ctOriginal = ctOriginal;
        this.ctMask = ctMask;
    }
}
