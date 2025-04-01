package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.grpc.TreatmentPlanningDto;
import ru.etu.controlservice.dto.task.TreatmentPlanningPayload;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.ResultPlanning;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.service.SegmentationNodeUpdater;
import ru.etu.controlservice.service.TreatmentPlanningClient;
import ru.etu.grpc.treatmentplanning.FinalAnatomicalStructure;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class TreatmentPlanningProcessor implements TaskProcessor {
    private final TreatmentPlanningClient treatmentPlanningClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final NodeRepository nodeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Task task) {
        try {
            TreatmentPlanningPayload payload = objectMapper.readValue(task.getPayload(), TreatmentPlanningPayload.class);
            Long resultPlanningId = payload.resultPlanningId();

            Node treatmentPlanningNode = task.getNode();
            if (treatmentPlanningNode == null) {
                throw new IllegalStateException("No Node associated with task " + task.getId());
            }

            Node resultPlanningNode =
                    nodeRepository.findByIdWithResultPlanningAndAlignmentSegmentation(resultPlanningId);

            if (resultPlanningNode == null) {
                throw new IllegalStateException("Required result planning not found");
            }

            ResultPlanning resultPlanning = resultPlanningNode.getResultPlanning();
            AlignmentSegmentation alignmentSegmentation = resultPlanning.getAlignmentSegmentation();
            List<String> stls = alignmentSegmentation.getStlToothRefs();
            List<String> initTeethMatrices = alignmentSegmentation.getInitTeethMatrices();
            List<String> desiredTeethMatrices = resultPlanning.getDesiredTeethMatrices();
            if (!(stls.size() == initTeethMatrices.size() && initTeethMatrices.size() == desiredTeethMatrices.size())) {
                throw new IllegalStateException("Length of required stls and init/desired teeth matrices not match");
            }

            List<FinalAnatomicalStructure> finalAnatomicalStructures = IntStream.range(0, stls.size())
                    .mapToObj(i -> FinalAnatomicalStructure.newBuilder()
                            .setStl(stls.get(i))
                            .setInitMatrix(initTeethMatrices.get(i))
                            .setDesiredMatrix(desiredTeethMatrices.get(i))
                            .build())
                    .toList();

            TreatmentPlanningDto treatmentPlanningDto = treatmentPlanningClient.planTreatment(finalAnatomicalStructures);
            if (treatmentPlanningDto == null) {
                log.error("TreatmentPlanning returned null for task {}", task.getId());
                throw new RuntimeException("TreatmentPlanning returned null result");
            }

            segmentationNodeUpdater.setTreatmentPlanning(treatmentPlanningNode, resultPlanning,
                    treatmentPlanningDto.collectionsOfMatricesGroups(), treatmentPlanningDto.attachments());

        } catch (Exception e) {
            log.error("TreatmentPlanning failed for task {}: {}", task.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process TreatmentPlanning task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.TREATMENT_PLANNING;
    }
}
