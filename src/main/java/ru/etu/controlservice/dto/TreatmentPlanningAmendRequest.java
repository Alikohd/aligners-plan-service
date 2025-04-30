package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record TreatmentPlanningAmendRequest(JsonNode treatmentStepMatrixGroup, JsonNode attachment) {
}
