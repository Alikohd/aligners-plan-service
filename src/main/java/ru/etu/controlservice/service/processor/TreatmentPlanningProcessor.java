package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.Struct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.grpc.TreatmentPlanningDto;
import ru.etu.controlservice.dto.task.TreatmentPlanningPayload;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.ResultPlanning;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.service.SegmentationNodeUpdater;
import ru.etu.controlservice.service.TreatmentPlanningClient;
import ru.etu.controlservice.util.NodeContentUtils;
import ru.etu.controlservice.util.ProtobufUtils;
import ru.etu.grpc.treatmentplanning.FinalAnatomicalStructure;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanningProcessor implements TaskProcessor {
    private final TreatmentPlanningClient treatmentPlanningClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final NodeRepository nodeRepository;
    private final NodeContentUtils nodeContentUtils;

    @Override
    public void process(Object payload, Node node) {
        try {
            TreatmentPlanningPayload treatmentPlanningPayload = (TreatmentPlanningPayload) payload;
            UUID resultPlanningId = treatmentPlanningPayload.resultPlanningId();

            log.info("Processing TREATMENT_PLANNING for node {}: resultPlanningId={}", node.getId(), resultPlanningId);

            Node resultPlanningNode = nodeRepository.findByIdWithResultPlanning(resultPlanningId)
                    .orElseThrow(() -> new IllegalStateException("ResultPlanning node not found: " + resultPlanningId));

            ResultPlanning resultPlanning = resultPlanningNode.getResultPlanning();
            if (resultPlanning == null) {
                throw new IllegalStateException("ResultPlanning not found for node " + resultPlanningId);
            }

            AlignmentSegmentation alignmentSegmentation = nodeContentUtils.getNodeWithType(
                    NodeType.SEGMENTATION_ALIGNMENT, resultPlanningNode.getId()).getAlignmentSegmentation();
            if (alignmentSegmentation == null) {
                throw new IllegalStateException("AlignmentSegmentation not found for node " + resultPlanningId);
            }

            List<String> stls = alignmentSegmentation.getToothRefs();
            List<JsonNode> initTeethMatrices = alignmentSegmentation.getInitTeethMatrices();
            List<JsonNode> desiredTeethMatrices = resultPlanning.getDesiredTeethMatrices();
            if (!(stls.size() == initTeethMatrices.size() && initTeethMatrices.size() == desiredTeethMatrices.size())) {
                throw new IllegalStateException("Length of required stls and init/desired teeth matrices do not match");
            }
            List<Struct> initMatricesStructs = ProtobufUtils.jsonNodesToStructs(initTeethMatrices);
            List<Struct> desiredMatricesStructs = ProtobufUtils.jsonNodesToStructs(desiredTeethMatrices);

            List<FinalAnatomicalStructure> finalAnatomicalStructures = IntStream.range(0, stls.size())
                    .mapToObj(i -> FinalAnatomicalStructure.newBuilder()
                            .setStl(stls.get(i))
                            .setInitMatrix(initMatricesStructs.get(i))
                            .setDesiredMatrix(desiredMatricesStructs.get(i))
                            .build())
                    .toList();

            TreatmentPlanningDto treatmentPlanningDto = treatmentPlanningClient.planTreatment(finalAnatomicalStructures);
            if (treatmentPlanningDto == null) {
                log.error("TreatmentPlanning returned null for node {}", node.getId());
                throw new RuntimeException("TreatmentPlanning returned null result");
            }

            segmentationNodeUpdater.setTreatmentPlanning(node,
                    treatmentPlanningDto.collectionsOfMatricesGroups(), treatmentPlanningDto.attachments());
        } catch (Exception e) {
            log.error("Failed to process TREATMENT_PLANNING task for node {}: {}", node.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process TREATMENT_PLANNING task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.TREATMENT_PLANNING;
    }
}