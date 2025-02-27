package ru.etu.controlservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private Long treatmentBranchId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "prev_node_id")
    private Node prevNode;

    @OneToMany(mappedBy = "prevNode")
    private List<Node> nextNodes = new ArrayList<>();

//    maybe redundant
    @OneToOne(mappedBy = "node")
    private CtSegmentation ctSegmentation;

    @OneToOne(mappedBy = "node")
    private JawSegmentation jawSegmentation;

    @OneToOne(mappedBy = "node")
    private AlignmentSegmentation alignmentSegmentation;

}
