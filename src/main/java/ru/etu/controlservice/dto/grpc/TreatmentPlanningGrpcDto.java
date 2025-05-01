package ru.etu.controlservice.dto.grpc;

import com.google.protobuf.Struct;

import java.util.List;

public record TreatmentPlanningGrpcDto(List<Struct> collectionsOfMatricesGroups, List<Struct> attachments) {
}
