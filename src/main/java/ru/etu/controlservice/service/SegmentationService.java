package ru.etu.controlservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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
import ru.etu.controlservice.util.NodeContentUtils;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final NodeContentUtils nodeContentUtils;
    private final List<NodeType> NODES_REQUIRED_FOR_ALIGNMENT = List.of(NodeType.SEGMENTATION_CT, NodeType.SEGMENTATION_JAW);

    @Transactional
    public NodePairDto prepareForAlignment(UUID patientId, UUID caseId, MultipartFile ctArchive,
                                           InputStream jawUpperStl, InputStream jawLowerStl) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);

        boolean segmentationStepAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getCtSegmentation() != null || node.getJawSegmentation() != null);
        if (segmentationStepAlreadyExists) {
            throw new StepAlreadyExistException("There is already some segmentation. Please use another API");
        }

        Node ctNode = nodeService.addStepToEnd(tCase);
        Node jawNode = nodeService.addStepToEnd(tCase);

//        maybe move saving into CtProcessor and JawProcessor due to long loading time (especially for PACS) in Transaction
//        likely possible with adding files validation
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
    public NodeDto startCtSegmentation(UUID patientId, UUID caseId, MultipartFile ctArchive) {
        log.debug("Starting ct segmentation");
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        log.debug("Case was retrieved");
        boolean ctAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getCtSegmentation() != null);
        log.debug("Traversing done");
        if (ctAlreadyExists) {
            throw new StepAlreadyExistException("There is already CtSegmentation. Use correction api to change it");
        }

        Node ctNode = nodeService.addStepToEnd(tCase);
        return pendCtTask(caseId, ctArchive, ctNode);
    }

    public NodeDto adjustCtInline(UUID patientId, UUID caseId, UUID nodeId, MultipartFile amendedCtMask) {
        caseService.getCaseById(patientId, caseId); // for validation
        Node currentCtNode = nodeService.getNode(nodeId);

        List<DicomDto> dicomDtos = pacsService.sendInstance(amendedCtMask, caseId);
        String ctCorrected = dicomDtos.get(0).parentSeries();
        currentCtNode.getCtSegmentation().getCtMask().setUri(ctCorrected);

        Node updatedNode = nodeService.updateNode(currentCtNode);
        return nodeMapper.toDto(updatedNode);
    }

    @Transactional
    public NodeDto adjustCt(UUID patientId, UUID caseId, UUID nodeId, MultipartFile ctArchive) {
        caseService.getCaseById(patientId, caseId);
        Node currentCtNode = nodeService.getNode(nodeId);
        Node newNode = nodeService.addStepTo(currentCtNode.getPrevNode());
        return pendCtTask(caseId, ctArchive, newNode);
    }

    public NodeDto adjustJawInline(UUID patientId, UUID caseId, UUID nodeId, List<JsonNode> amendedJawsSegmented) {
        caseService.getCaseById(patientId, caseId);
        Node currentJawNode = nodeService.getNode(nodeId);
        currentJawNode.getJawSegmentation().setJawsSegmented(amendedJawsSegmented);
        Node updatedNode = nodeService.updateNode(currentJawNode);
        return nodeMapper.toDto(updatedNode);
    }

    @Transactional
    public NodeDto adjustJaw(UUID patientId, UUID caseId, UUID nodeId, InputStream jawUpperStl, InputStream jawLowerStl) {
        caseService.getCaseById(patientId, caseId);
        Node currentJawNode = nodeService.getNode(nodeId);
        Node newNode = nodeService.addStepTo(currentJawNode.getPrevNode());
        return pendJawTask(patientId, caseId, jawUpperStl, jawLowerStl, newNode);
    }

    @Transactional
    public NodeDto startJawSegmentation(UUID patientId, UUID caseId, InputStream jawUpperStl, InputStream jawLowerStl) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        boolean jawAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getJawSegmentation() != null);
        if (jawAlreadyExists) {
            throw new StepAlreadyExistException("There is already JawSegmentation. Use correction api to change it");
        }

        Node jawNode = nodeService.addStepToEnd(tCase);
        return pendJawTask(patientId, caseId, jawUpperStl, jawLowerStl, jawNode);
    }

    public NodeDto adjustAlignmentInline(UUID patientId, UUID caseId, UUID nodeId, List<JsonNode> amendedInitTeethMatrices) {
        caseService.getCaseById(patientId, caseId);
        Node currentAlignmentNode = nodeService.getNode(nodeId);
        currentAlignmentNode.getAlignmentSegmentation().setInitTeethMatrices(amendedInitTeethMatrices);
        Node updatedNode = nodeService.updateNode(currentAlignmentNode);
        return nodeMapper.toDto(updatedNode);
    }

    @Transactional
    public NodeDto adjustAlignment(UUID patientId, UUID caseId, UUID nodeId) {
        caseService.getCaseById(patientId, caseId);
        Node currentAlignmentNode = nodeService.getNode(nodeId);
        Node newNode = nodeService.addStepTo(currentAlignmentNode.getPrevNode());
        return pendAlignmentTask(newNode);
    }

    @Transactional
    public NodeDto startAlignment(UUID patientId, UUID caseId) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        boolean alignmentAlreadyExists = nodeService.traverseNodes(tCase.getRoot())
                .anyMatch(node -> node.getAlignmentSegmentation() != null);
        if (alignmentAlreadyExists) {
            throw new StepAlreadyExistException("There is already Alignment. Use correction api to change it");
        }

        Node alignmentNode = nodeService.addStepToEnd(tCase);
        return pendAlignmentTask(alignmentNode);
    }

    private NodeDto pendCtTask(UUID caseId, MultipartFile ctOriginal, Node newNode) {
        List<DicomDto> dicomDtos = pacsService.sendInstance(ctOriginal, caseId);
        String ctCorrected = dicomDtos.get(0).parentSeries();

        SegmentationCtPayload payload = new SegmentationCtPayload(ctCorrected);
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        taskService.addTask(payloadJson, NodeType.SEGMENTATION_CT, newNode);
        return nodeMapper.toDto(newNode);
    }

    private NodeDto pendJawTask(UUID patientId, UUID caseId, InputStream jawUpperStl, InputStream jawLowerStl, Node newNode) {
        String jawUpperStlSaved = fileService.saveFile(jawUpperStl, patientId, caseId);
        String jawLowerStlSaved = fileService.saveFile(jawLowerStl, patientId, caseId);

        SegmentationJawPayload payload = new SegmentationJawPayload(jawUpperStlSaved, jawLowerStlSaved);
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }
        taskService.addTask(payloadJson, NodeType.SEGMENTATION_JAW, newNode);
        return nodeMapper.toDto(newNode);
    }

    private NodeDto pendAlignmentTask(Node newNode) {
        Map<NodeType, Node> prevSegmentationNodes = nodeContentUtils.getPrevNodes(newNode, NODES_REQUIRED_FOR_ALIGNMENT);
        if (prevSegmentationNodes.size() != NODES_REQUIRED_FOR_ALIGNMENT.size()) {
            throw new NodesRequiredForAlignmentNotFoundException("Nodes required for alignment were not found!");
        }

        AlignmentPayload payload = new AlignmentPayload(
                prevSegmentationNodes.get(NodeType.SEGMENTATION_CT).getId(),
                prevSegmentationNodes.get(NodeType.SEGMENTATION_JAW).getId());

        try {
            taskService.addTask(objectMapper.writeValueAsString(payload), NodeType.SEGMENTATION_ALIGNMENT, newNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        return nodeMapper.toDto(newNode);
    }
}
