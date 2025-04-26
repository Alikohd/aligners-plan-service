package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record TreatmentPlanningAmendRequest(UUID node, JsonNode treatmentStepMatrixGroup, JsonNode attachment) {
}
