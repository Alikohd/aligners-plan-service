package ru.etu.controlservice.entity.id;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodePrevRelationId implements Serializable {
    private Long node;
    private Long prevNode;
}
