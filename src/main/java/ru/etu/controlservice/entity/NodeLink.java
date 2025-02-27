//package ru.etu.controlservice.entity;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Getter
//@Setter
//public class NodeLink {
//    @Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "prev_node_id")
//    private TreatmentNode prevNode;
//
//    @ManyToOne
//    @JoinColumn(name = "next_node_id")
//    private TreatmentNode nextNode;
//}
