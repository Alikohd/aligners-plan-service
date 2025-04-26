package ru.etu.controlservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.etu.controlservice.entity.NodeType;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlatNodeDto {
    UUID id;
    NodeType type;
    List<UUID> childrenIds;
}