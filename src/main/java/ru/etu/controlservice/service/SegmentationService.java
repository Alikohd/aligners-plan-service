package ru.etu.controlservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomDto;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.dto.NodePairDto;
import ru.etu.controlservice.dto.task.AlignmentPayload;
import ru.etu.controlservice.dto.task.SegmentationCtPayload;
import ru.etu.controlservice.dto.task.SegmentationJawPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.NodesRequiredForAlignmentNotFoundException;
import ru.etu.controlservice.exceptions.StepAlreadyExistException;
import ru.etu.controlservice.mapper.NodeMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class SegmentationService {
    private final TreatmentCaseService caseService;
    private final NodeService nodeService;
    private final PacsService pacsService;
    private final FileService fileService;
    private final NodeMapper nodeMapper;
    private final TaskService taskService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<NodeType> NODES_REQUIRED_FOR_ALIGNMENT = List.of(NodeType.SEGMENTATION_CT, NodeType.SEGMENTATION_JAW);

    @Transactional
    public NodePairDto prepareForAlignment(Long patientId, Long caseId, MultipartFile ctArchive,
                                           InputStream jawUpperStl, InputStream jawLowerStl) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);

        boolean segmentationStepAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getCtSegmentation() != null || node.getJawSegmentation() != null);
        if (segmentationStepAlreadyExists) {
            throw new StepAlreadyExistException("There is already some segmentation step in latest branch. Please use another API");
        }

        Node ctNode = nodeService.addStep(tCase);
        Node jawNode = nodeService.addStep(tCase);

//        maybe move saving into CtProcessor and JawProcessor due to long loading time (especially for PACS) in Transaction
        List<DicomDto> dicomDtos = pacsService.sendInstance(ctArchive, caseId);
        String jawUpperStlSaved = fileService.saveFile(jawUpperStl, patientId, caseId);
        String jawLowerStlSaved = fileService.saveFile(jawLowerStl, patientId, caseId);
        String ctOriginal = dicomDtos.get(0).parentSeries();

        NodePairDto initialResult = new NodePairDto(
                nodeMapper.toDto(ctNode),
                nodeMapper.toDto(jawNode)
        );

        SegmentationCtPayload ctPayload = new SegmentationCtPayload(ctOriginal);
        SegmentationJawPayload jawPayload = new SegmentationJawPayload(jawUpperStlSaved, jawLowerStlSaved);

        try {
            taskService.addTask(objectMapper.writeValueAsString(ctPayload), NodeType.SEGMENTATION_CT, ctNode);
            taskService.addTask(objectMapper.writeValueAsString(jawPayload), NodeType.SEGMENTATION_JAW, jawNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        return initialResult;
    }

    @Transactional
    public NodeDto startCtSegmentation(Long patientId, Long caseId, MultipartFile ctArchive) {
        log.debug("Starting ct segmentation");
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        log.debug("Case was retrieved");
        boolean ctAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getCtSegmentation() != null);
        log.debug("Traversing done");
        if (ctAlreadyExists) {
            throw new StepAlreadyExistException("There is already CtSegmentation in latest branch. Use correction api to change it");
        }

        Node ctNode = nodeService.addStep(tCase);
        List<DicomDto> dicomDtos = pacsService.sendInstance(ctArchive, caseId);
        String ctOriginal = dicomDtos.get(0).parentSeries();

        SegmentationCtPayload payload = new SegmentationCtPayload(ctOriginal);
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        taskService.addTask(payloadJson, NodeType.SEGMENTATION_CT, ctNode);

        return nodeMapper.toDto(ctNode);
    }

    @Transactional
    public NodeDto startJawSegmentation(Long patientId, Long caseId, InputStream jawUpperStl, InputStream jawLowerStl) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);

        boolean jawAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getJawSegmentation() != null);
        if (jawAlreadyExists) {
            throw new StepAlreadyExistException("There is already JawSegmentation in latest branch. Use correction api to change it");
        }

        Node jawNode = nodeService.addStep(tCase);

        String jawUpperStlSaved = fileService.saveFile(jawUpperStl, patientId, caseId);
        String jawLowerStlSaved = fileService.saveFile(jawLowerStl, patientId, caseId);

        SegmentationJawPayload payload = new SegmentationJawPayload(jawUpperStlSaved, jawLowerStlSaved);
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        taskService.addTask(payloadJson, NodeType.SEGMENTATION_JAW, jawNode);

        return nodeMapper.toDto(jawNode);
    }

    @Transactional
    public NodeDto startAlignment(Long patientId, Long caseId) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        boolean alignmentAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getAlignmentSegmentation() != null);
        if (alignmentAlreadyExists) {
            throw new StepAlreadyExistException("There is already Alignment in latest branch. Use correction api to change it");
        }

        Node alignmentNode = nodeService.addStep(tCase);
        Map<NodeType, Node> prevSegmentationNodes = Stream.iterate(alignmentNode,
                        node -> !node.getPrevNodes().isEmpty(),
                        node -> node.getPrevNodes().get(0).getPrevNode())
                .flatMap(node -> NODES_REQUIRED_FOR_ALIGNMENT.stream()
                        .filter(type -> type.getNodeStep(node) != null)
                        .map(type -> Map.entry(type, node)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (prevSegmentationNodes.size() != NODES_REQUIRED_FOR_ALIGNMENT.size()) {
            throw new NodesRequiredForAlignmentNotFoundException("Nodes required for alignment were not found!");
        }

        AlignmentPayload payload = new AlignmentPayload(
                prevSegmentationNodes.get(NodeType.SEGMENTATION_CT).getId(),
                prevSegmentationNodes.get(NodeType.SEGMENTATION_JAW).getId());

        try {
            taskService.addTask(objectMapper.writeValueAsString(payload), NodeType.SEGMENTATION_ALIGNMENT, alignmentNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        return nodeMapper.toDto(alignmentNode);
    }

}
