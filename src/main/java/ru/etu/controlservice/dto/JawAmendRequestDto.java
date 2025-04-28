package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record JawAmendRequestDto(List<JsonNode> amendedJawsSegmented) {
}