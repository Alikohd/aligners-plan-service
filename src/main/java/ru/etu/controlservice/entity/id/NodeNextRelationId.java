package ru.etu.controlservice.entity.id;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeNextRelationId implements Serializable {
    private Long node;
    private Long nextNode;
}
