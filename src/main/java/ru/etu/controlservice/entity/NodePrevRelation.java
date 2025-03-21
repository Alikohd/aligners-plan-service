package ru.etu.controlservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import ru.etu.controlservice.entity.id.NodePrevRelationId;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "node_prev_relation")
@IdClass(NodePrevRelationId.class)
public class NodePrevRelation {
    @Id
    @ManyToOne
    @JoinColumn(name = "node_id", nullable = false)
    private Node node;

    @Id
    @ManyToOne
    @JoinColumn(name = "prev_node_id", nullable = false)
    private Node prevNode;
}
