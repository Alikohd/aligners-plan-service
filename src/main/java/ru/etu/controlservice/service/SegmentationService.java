package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomDto;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.exceptions.NodesRequiredForAlignmentNotFoundException;
import ru.etu.controlservice.mapper.NodeMapper;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.util.SegmentationTypeRequiredForAlignment;
import ru.etu.grpc.segmentation.AnatomicalStructure;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SegmentationService {
    private final TreatmentCaseService caseService;
    private final NodeService nodeService;
    private final PacsService pacsService;
    private final FileService fileService;
    private final NodeRepository nodeRepository;
    private final NodeMapper nodeMapper;
    private final SegmentationClient segmentationClient;

    @Transactional
    public NodeDto startCtSegmentation(Long patientId, Long caseId, MultipartFile ctArchive) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        List<DicomDto> dicomDtos = pacsService.sendInstance(ctArchive, caseId);
        Node ctNode = nodeService.createStep(tCase);

        String ctOriginal = dicomDtos.get(0).parentSeries();
        String ctSegmentationResponse = segmentationClient.segmentCt(ctOriginal);

        setCtSegmentation(ctNode, ctOriginal, ctSegmentationResponse);
        Node persistedNode = nodeRepository.save(ctNode);
        return nodeMapper.toDto(persistedNode);
    }

    @Transactional
    public NodeDto startJawSegmentation(Long patientId, Long caseId, InputStream jawUpperStl, InputStream jawLowerStl) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        Node jawNode = nodeService.createStep(tCase);

        String jawUpperStlSaved = fileService.saveFile(jawUpperStl, patientId, caseId);
        String jawLowerStlSaved = fileService.saveFile(jawLowerStl, patientId, caseId);
        List<String> jawSegmentationResponse = segmentationClient.segmentJaw(jawUpperStlSaved, jawLowerStlSaved);

        setJawSegmentation(jawNode, jawUpperStlSaved, jawLowerStlSaved, jawSegmentationResponse);
        Node persistedNode = nodeRepository.save(jawNode);
        return nodeMapper.toDto(persistedNode);
    }

    @Transactional
    public NodeDto startAlignment(Long patientId, Long caseId) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        Node alignmentNode = nodeService.createStep(tCase);
        Map<SegmentationTypeRequiredForAlignment, Node> prevSegmentationNodes = Stream.iterate(alignmentNode,
                        node -> !node.getPrevNodes().isEmpty(),
                        node -> node.getPrevNodes().get(0).getPrevNode())
                .flatMap(node -> Arrays.stream(SegmentationTypeRequiredForAlignment.values())
                        .filter(type -> type.getSegmentation(node) != null)
                        .map(type -> Map.entry(type, node)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (prevSegmentationNodes.size() != SegmentationTypeRequiredForAlignment.values().length) {
            throw new NodesRequiredForAlignmentNotFoundException("Nodes required for alignment were not found!");
        }

        CtSegmentation ctSegmentation = prevSegmentationNodes.get(SegmentationTypeRequiredForAlignment.CT).getCtSegmentation();
        JawSegmentation jawSegmentation = prevSegmentationNodes.get(SegmentationTypeRequiredForAlignment.JAW).getJawSegmentation();

        List<AnatomicalStructure> alignmentSegmentationResponse = segmentationClient.align(ctSegmentation.getCtMask(),
                jawSegmentation.getJawUpperStl(), jawSegmentation.getJawLowerStl(), jawSegmentation.getJawsJson());

        List<String> stls = alignmentSegmentationResponse.stream()
                .map(AnatomicalStructure::getStl)
                .toList();
        List<String> initMatrices = alignmentSegmentationResponse.stream()
                .map(AnatomicalStructure::getInitMatrix)
                .toList();

        setAlignmentSegmentation(alignmentNode, ctSegmentation, jawSegmentation, stls, initMatrices);
        Node persistedNode = nodeRepository.save(alignmentNode);
        return nodeMapper.toDto(persistedNode);
    }

    private void setCtSegmentation(Node node, String ctOriginal, String ctMask) {
        CtSegmentation ctSegmentation = CtSegmentation.builder()
                .ctOriginal(ctOriginal)
                .ctMask(ctMask)
                .build();
        node.setCtSegmentation(ctSegmentation);
    }

    private void setJawSegmentation(Node node, String jawUpperStl, String jawLowerStl, List<String> jawsJson) {
        JawSegmentation jawSegmentation = JawSegmentation.builder()
                .jawUpperStl(jawUpperStl)
                .jawLowerStl(jawLowerStl)
                .jawsJson(jawsJson)
                .build();
        node.setJawSegmentation(jawSegmentation);
    }

    private void setAlignmentSegmentation(Node node, CtSegmentation ctSegmentation, JawSegmentation jawSegmentation,
                                          List<String> stlToothRefs, List<String> initTeethMatrices) {
        AlignmentSegmentation alignmentSegmentation = AlignmentSegmentation.builder()
                .ctSegmentation(ctSegmentation)
                .jawSegmentation(jawSegmentation)
                .initTeethMatrices(initTeethMatrices)
                .stlToothRefs(stlToothRefs)
                .build();
        node.setAlignmentSegmentation(alignmentSegmentation);
    }
}
