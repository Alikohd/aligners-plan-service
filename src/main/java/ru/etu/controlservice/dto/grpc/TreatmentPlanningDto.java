package ru.etu.controlservice.dto.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.Struct;

import java.util.List;

public record TreatmentPlanningDto(List<Struct> collectionsOfMatricesGroups, List<Struct> attachments) {
}
