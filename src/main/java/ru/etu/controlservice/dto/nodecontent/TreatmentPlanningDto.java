package ru.etu.controlservice.dto.nodecontent;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record TreatmentPlanningDto(UUID id, List<JsonNode> treatmentStepMatrixGroup, List<JsonNode> attachment) {
}
