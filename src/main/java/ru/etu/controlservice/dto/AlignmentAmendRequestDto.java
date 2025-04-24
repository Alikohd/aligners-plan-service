package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record AlignmentAmendRequestDto(UUID node, List<JsonNode> amendedInitTeethMatrices) {
}