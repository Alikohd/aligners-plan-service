package ru.etu.controlservice.dto;

import java.time.Instant;

public record ErrorResponse(Integer statusCode, String message, Instant timestamp) {
}
