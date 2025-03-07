package ru.etu.controlservice.dto.grpc;

import java.util.List;

public record TreatmentPlanningDto(List<String> collectionsOfMatricesGroups, List<String> attachments) {
}
