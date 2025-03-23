package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.etu.controlservice.dto.DicomDto;
import ru.etu.controlservice.dto.NodeDto;
import ru.etu.controlservice.entity.CtSegmentation;
import ru.etu.controlservice.entity.JawSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.mapper.NodeMapper;
import ru.etu.controlservice.repository.NodeRepository;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentationService {
    private final TreatmentCaseService caseService;
    private final NodeService nodeService;
    private final PacsService pacsService;
    private final FileService fileService;
    private final NodeRepository nodeRepository;
    private final NodeMapper nodeMapper;

    @Transactional
    public NodeDto startCtSegmentation(Long patientId, Long caseId, MultipartFile ctArchive) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        List<DicomDto> dicomDtos = pacsService.sendInstance(ctArchive, caseId);
        Node ctNode = nodeService.createStep(tCase);
//        todo: запрос в сервис сегментации на кт
        String mockSegmentationAnswer = "mockSegmentationAnswer";
        setCtSegmentation(ctNode, dicomDtos.get(0).parentSeries(), mockSegmentationAnswer);

        Node persistedNode = nodeRepository.save(ctNode);
        return nodeMapper.toDto(persistedNode);
    }

    @Transactional
    public Node startJawSegmentation(Long patientId, Long caseId, InputStream jawUpperStl, InputStream jawLowerStl) {
        TreatmentCase tCase = caseService.getCaseById(patientId, caseId);
        Node jawNode = nodeService.createStep(tCase);

        String jawUpperStlSaved = fileService.saveFile(jawUpperStl, patientId, caseId);
        String jawLowerStlSaved = fileService.saveFile(jawLowerStl, patientId, caseId);
//        todo: запрос в сервис сегментации на челюсти
        List<String> mockSegmentationAnswer = List.of("mockSegmentationAnswer");

        setJawSegmentation(jawNode, jawUpperStlSaved, jawLowerStlSaved, mockSegmentationAnswer);

        return nodeRepository.save(jawNode);
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
}
