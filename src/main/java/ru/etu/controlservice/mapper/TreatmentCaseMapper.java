package ru.etu.controlservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.etu.controlservice.dto.TreatmentCaseDto;
import ru.etu.controlservice.entity.TreatmentCase;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TreatmentCaseMapper {

    @Mapping(target = "patientId", source = "patient.id")
    @Mapping(source = "root.id", target = "rootId")
    TreatmentCaseDto entityToDto(TreatmentCase tCase);
}
