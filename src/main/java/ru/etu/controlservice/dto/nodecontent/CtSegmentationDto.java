package ru.etu.controlservice.dto.nodecontent;

import java.util.UUID;

public record CtSegmentationDto(UUID id, UUID ctOriginalId, UUID ctMaskId) {
}
