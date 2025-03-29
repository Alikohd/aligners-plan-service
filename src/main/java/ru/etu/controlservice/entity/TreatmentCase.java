package ru.etu.controlservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NamedEntityGraph(
        name = "case-with-nextnodes",
        attributeNodes = {
                @NamedAttributeNode(value = "root", subgraph = "root-nodes")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "root-nodes",
                        attributeNodes = {
                                @NamedAttributeNode("id"),
                                @NamedAttributeNode("nextNodes")
                        }
                )
        }
)
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "treatment_case")
public class TreatmentCase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "root_id")
    private Node root;

}
