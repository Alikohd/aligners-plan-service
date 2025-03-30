package ru.etu.controlservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.etu.controlservice.dto.PatientDto;
import ru.etu.controlservice.entity.Patient;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PatientMapper {

    PatientDto entityToDto(Patient patient);
}
