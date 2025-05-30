package ru.etu.controlservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.etu.controlservice.dto.nodecontent.AlignmentSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.CtSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.JawSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.ResultPlanningDto;
import ru.etu.controlservice.dto.nodecontent.TreatmentPlanningDto;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.ResultPlanning;
import ru.etu.controlservice.entity.TreatmentPlanning;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NodeContentMapper {

    @Mapping(source = "ctOriginal.id", target = "ctOriginalId")
    @Mapping(source = "ctMask.id", target = "ctMaskId")
    CtSegmentationDto toCtSegmentationDto(CtSegmentation entity);

    @Mapping(source = "jawUpper.id", target = "jawUpperId")
    @Mapping(source = "jawLower.id", target = "jawLowerId")
    JawSegmentationDto toJawSegmentationDto(JawSegmentation entity);

    @Mapping(source = "toothRefs", target = "toothRefs", qualifiedByName = "mapToothRefsToUris")
    AlignmentSegmentationDto toAlignmentSegmentationDto(AlignmentSegmentation alignmentSegmentation);

    @Named("mapToothRefsToUris")
    default List<UUID> mapToothRefsToUris(List<File> toothRefs) {
        if (toothRefs == null) {
            return null;
        }
        return toothRefs.stream()
                .map(File::getId)
                .toList();
    }

    ResultPlanningDto toResultPlanningDto(ResultPlanning resultPlanning);

    TreatmentPlanningDto toTreatmentPlanningDto(TreatmentPlanning resultPlanning);
}
