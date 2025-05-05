package ru.etu.controlservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.NodeNotFoundException;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.repository.TreatmentCaseRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NodeServiceTest {

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private TreatmentCaseRepository treatmentCaseRepository;

    @InjectMocks
    private NodeService nodeService;

    @Test
    void addStepToEnd_WhenRootIsNull_ShouldCreateInitialNode() {
        TreatmentCase treatmentCase = new TreatmentCase();
        treatmentCase.setRoot(null);

        Node result = nodeService.addStepToEnd(treatmentCase);

        assertNotNull(result);
        assertEquals(result, treatmentCase.getRoot());
        verify(treatmentCaseRepository).save(treatmentCase);
    }

    @Test
    void addStepToEnd_WhenRootExists_ShouldAddStepToLastNode() {
        Node root = new Node();
        root.setNextNodes(new ArrayList<>());

        TreatmentCase treatmentCase = new TreatmentCase();
        treatmentCase.setRoot(root);

        Node savedNode = new Node();
        when(nodeRepository.save(any(Node.class))).thenReturn(savedNode);

        Node result = nodeService.addStepToEnd(treatmentCase);

        assertEquals(savedNode, result);
        verify(nodeRepository, times(2)).save(any(Node.class));
    }

    @Test
    void findLastNode_ShouldReturnTheLastNodeInSequence() {
        Node n1 = new Node();
        n1.setCreatedAt(OffsetDateTime.parse("2023-01-01T00:00:00Z").toLocalDateTime());
        Node n2 = new Node();
        n2.setCreatedAt(OffsetDateTime.parse("2023-01-02T00:00:00Z").toLocalDateTime());
        Node n3 = new Node();
        n3.setCreatedAt(OffsetDateTime.parse("2023-01-03T00:00:00Z").toLocalDateTime());

        n1.setNextNodes(List.of(n2));
        n2.setNextNodes(List.of(n3));
        n3.setNextNodes(Collections.emptyList());

        Node last = nodeService.findLastNode(n1);

        assertEquals(n3, last);
    }

    @Test
    void addStepTo_ShouldSaveNodes() {
        Node prev = new Node();
        prev.setNextNodes(new ArrayList<>());

        Node newNode = new Node();
        newNode.setPrevNode(prev); // Expected to be set

        when(nodeRepository.save(any(Node.class))).thenReturn(newNode);

        Node result = nodeService.addStepTo(prev);

        assertEquals(newNode, result);
        assertEquals(prev, newNode.getPrevNode());
        verify(nodeRepository, times(2)).save(any(Node.class));
    }

    @Test
    void updateNode_ShouldDelegateToRepository() {
        Node node = new Node();
        when(nodeRepository.save(node)).thenReturn(node);

        Node updated = nodeService.updateNode(node);

        assertEquals(node, updated);
        verify(nodeRepository).save(node);
    }

    @Test
    void getNode_WhenExists_ShouldReturnNode() {
        UUID id = UUID.randomUUID();
        Node node = new Node();
        when(nodeRepository.findById(id)).thenReturn(Optional.of(node));

        Node result = nodeService.getNode(id);

        assertEquals(node, result);
        verify(nodeRepository).findById(id);
    }

    @Test
    void getNode_WhenNotFound_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(nodeRepository.findById(id)).thenReturn(Optional.empty());

        NodeNotFoundException ex = assertThrows(NodeNotFoundException.class, () -> nodeService.getNode(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }
}
