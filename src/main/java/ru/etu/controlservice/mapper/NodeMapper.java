package ru.etu.controlservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.etu.controlservice.dto.MetaNodeDto;
import ru.etu.controlservice.entity.Node;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NodeMapper {

    MetaNodeDto toDto(Node node);
}
