package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ct_segmentation")
public class CtSegmentation extends BaseTreatmentStep {
    @Column(name = "ct_original")
    private String ctOriginal;

    @Column(name = "ct_mask")
    private String ctMask;

}
