package ru.etu.controlservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.etu.controlservice.dto.FlatNodeDto;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeGraphServiceTest {

    @Mock
    private TreatmentCaseService treatmentCaseService;

    @InjectMocks
    private NodeGraphService nodeGraphService;

    private UUID patientId;
    private UUID caseId;

    @BeforeEach
    void setup() {
        patientId = UUID.randomUUID();
        caseId = UUID.randomUUID();
    }

    @Test
    void getFlatGraph_WhenRootIsNull_ShouldReturnEmptyList() {
        TreatmentCase emptyCase = new TreatmentCase();
        emptyCase.setRoot(null);

        when(treatmentCaseService.getCaseById(patientId, caseId)).thenReturn(emptyCase);

        List<FlatNodeDto> result = nodeGraphService.getFlatGraph(patientId, caseId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getFlatGraph_WithSimpleGraph_ShouldReturnCorrectFlatDtoList() {
        UUID node1Id = UUID.randomUUID();
        UUID node2Id = UUID.randomUUID();

        Node node1 = new Node();
        node1.setId(node1Id);
        node1.setNextNodes(new ArrayList<>());

        Node node2 = new Node();
        node2.setId(node2Id);
        node2.setNextNodes(new ArrayList<>());

        // node1 -> node2
        node1.getNextNodes().add(node2);

        TreatmentCase treatmentCase = new TreatmentCase();
        treatmentCase.setRoot(node1);

        when(treatmentCaseService.getCaseById(patientId, caseId)).thenReturn(treatmentCase);

        List<FlatNodeDto> result = nodeGraphService.getFlatGraph(patientId, caseId);

        assertEquals(2, result.size());

        FlatNodeDto rootDto = result.stream()
                .filter(dto -> dto.getId().equals(node1Id))
                .findFirst()
                .orElseThrow();

        FlatNodeDto childDto = result.stream()
                .filter(dto -> dto.getId().equals(node2Id))
                .findFirst()
                .orElseThrow();

        assertEquals(List.of(node2Id), rootDto.getChildrenIds());
        assertEquals(Collections.emptyList(), childDto.getChildrenIds());
    }

    @Test
    void getFlatGraph_WithCyclicGraph_ShouldNotLoopInfinitely() {
        UUID node1Id = UUID.randomUUID();
        UUID node2Id = UUID.randomUUID();

        Node node1 = new Node();
        node1.setId(node1Id);
        node1.setNextNodes(new ArrayList<>());

        Node node2 = new Node();
        node2.setId(node2Id);
        node2.setNextNodes(new ArrayList<>());

        // Цикл: node1 -> node2 -> node1
        node1.getNextNodes().add(node2);
        node2.getNextNodes().add(node1);

        TreatmentCase treatmentCase = new TreatmentCase();
        treatmentCase.setRoot(node1);

        when(treatmentCaseService.getCaseById(patientId, caseId)).thenReturn(treatmentCase);

        List<FlatNodeDto> result = nodeGraphService.getFlatGraph(patientId, caseId);

        assertEquals(2, result.size());

        Set<UUID> ids = new HashSet<>();
        result.forEach(dto -> ids.add(dto.getId()));
        assertTrue(ids.containsAll(List.of(node1Id, node2Id)));
    }
}
