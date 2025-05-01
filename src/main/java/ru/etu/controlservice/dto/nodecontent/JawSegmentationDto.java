package ru.etu.controlservice.dto.nodecontent;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record JawSegmentationDto(UUID id, UUID jawUpperId, UUID jawLowerId, List<JsonNode> jawsSegmented) {
}
