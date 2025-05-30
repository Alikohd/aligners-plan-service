package ru.etu.controlservice.dto.nodecontent;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record ResultPlanningDto(UUID id, List<JsonNode> desiredTeethMatrices) {
}
