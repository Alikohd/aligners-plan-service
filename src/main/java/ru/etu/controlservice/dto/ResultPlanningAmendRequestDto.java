package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record ResultPlanningAmendRequestDto(List<JsonNode> amendedDesiredTeethMatrices) {
}
