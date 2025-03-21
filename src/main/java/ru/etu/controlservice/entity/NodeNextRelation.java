package ru.etu.controlservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.etu.controlservice.entity.id.NodeNextRelationId;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "node_next_relation")
@IdClass(NodeNextRelationId.class)
public class NodeNextRelation {
    @Id
    @ManyToOne
    @JoinColumn(name = "node_id", nullable = false)
    private Node node;

    @Id
    @ManyToOne
    @JoinColumn(name = "next_node_id", nullable = false)
    private Node nextNode;
}
