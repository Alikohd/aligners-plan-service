package ru.etu.controlservice.service;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.etu.controlservice.dto.nodecontent.AlignmentSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.CtSegmentationDto;
import ru.etu.controlservice.dto.nodecontent.JawSegmentationDto;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.File;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.mapper.NodeContentMapper;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NodeContentMapperTest {

    private final NodeContentMapper mapper = Mappers.getMapper(NodeContentMapper.class);

    @Test
    void toCtSegmentationDto_ShouldMapIdsCorrectly() {
        UUID originalId = UUID.randomUUID();
        UUID maskId = UUID.randomUUID();

        File ctOriginal = new File();
        ctOriginal.setId(originalId);

        File ctMask = new File();
        ctMask.setId(maskId);

        CtSegmentation entity = new CtSegmentation();
        entity.setCtOriginal(ctOriginal);
        entity.setCtMask(ctMask);

        CtSegmentationDto dto = mapper.toCtSegmentationDto(entity);

        assertEquals(originalId, dto.ctOriginalId());
        assertEquals(maskId, dto.ctMaskId());
    }

    @Test
    void toJawSegmentationDto_ShouldMapUpperAndLowerIds() {
        UUID upperId = UUID.randomUUID();
        UUID lowerId = UUID.randomUUID();

        File upper = new File();
        upper.setId(upperId);

        File lower = new File();
        lower.setId(lowerId);

        JawSegmentation entity = new JawSegmentation();
        entity.setJawUpper(upper);
        entity.setJawLower(lower);

        JawSegmentationDto dto = mapper.toJawSegmentationDto(entity);

        assertEquals(upperId, dto.jawUpperId());
        assertEquals(lowerId, dto.jawLowerId());
    }

    @Test
    void toAlignmentSegmentationDto_ShouldMapToothRefsToUUIDs() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        File file1 = new File();
        file1.setId(id1);

        File file2 = new File();
        file2.setId(id2);

        AlignmentSegmentation entity = new AlignmentSegmentation();
        entity.setToothRefs(List.of(file1, file2));

        AlignmentSegmentationDto dto = mapper.toAlignmentSegmentationDto(entity);

        assertNotNull(dto.toothRefs());
        assertEquals(List.of(id1, id2), dto.toothRefs());
    }

    @Test
    void toAlignmentSegmentationDto_WithNullList_ShouldReturnNull() {
        AlignmentSegmentation entity = new AlignmentSegmentation();
        entity.setToothRefs(null);

        AlignmentSegmentationDto dto = mapper.toAlignmentSegmentationDto(entity);

        assertNull(dto.toothRefs());
    }
}

