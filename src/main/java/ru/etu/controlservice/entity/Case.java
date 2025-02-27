//package ru.etu.controlservice.entity;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//
//import java.util.UUID;
//
//@Entity
//public class Case {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID id;
//
//    @ManyToOne
//    @JoinColumn(name = "patient_id", nullable = false)
//    private Patient patient;
//}
