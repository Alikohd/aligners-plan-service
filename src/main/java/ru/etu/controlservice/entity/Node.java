package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;

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

    @OneToMany(mappedBy = "node")
    private List<NodePrevRelation> prevNodes = new ArrayList<>();

    @OneToMany(mappedBy = "node")
    private List<NodeNextRelation> nextNodes = new ArrayList<>();

//    maybe redundant
    @OneToOne(mappedBy = "node")
    private CtSegmentation ctSegmentation;

    @OneToOne(mappedBy = "node")
    private JawSegmentation jawSegmentation;

    @OneToOne(mappedBy = "node")
    private AlignmentSegmentation alignmentSegmentation;

    @OneToOne(mappedBy = "node")
    private ResultPlanning resultPlanning;

    @OneToOne(mappedBy = "node")
    private TreatmentPlanning treatmentPlanning;

}
