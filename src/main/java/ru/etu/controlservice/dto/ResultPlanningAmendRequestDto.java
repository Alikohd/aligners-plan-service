package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record ResultPlanningAmendRequestDto(UUID node, List<JsonNode> amendedDesiredTeethMatrices) {
}
