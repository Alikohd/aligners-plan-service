package ru.etu.controlservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "node")
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Long treatmentBranchId;

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL)
    @Builder.Default
    private List<NodePrevRelation> prevNodes = new ArrayList<>();

    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL)
    @Builder.Default
    private List<NodeNextRelation> nextNodes = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "сt_segmentation_id")
    private CtSegmentation ctSegmentation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "jaw_segmentation_id")
    private JawSegmentation jawSegmentation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "alignment_segmentation_id")
    private AlignmentSegmentation alignmentSegmentation;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "result_planning_id")
    private ResultPlanning resultPlanning;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_planning_id")
    private TreatmentPlanning treatmentPlanning;

}
