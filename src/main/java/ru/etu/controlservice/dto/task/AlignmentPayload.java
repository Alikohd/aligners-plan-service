package ru.etu.controlservice.dto.task;

import java.util.UUID;

public record AlignmentPayload(UUID ctNodeId, UUID jawNodeId) {
}
