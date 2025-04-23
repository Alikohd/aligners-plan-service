package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record JawAmendRequest(UUID node, List<JsonNode> amendedJawsSegmented) {
}