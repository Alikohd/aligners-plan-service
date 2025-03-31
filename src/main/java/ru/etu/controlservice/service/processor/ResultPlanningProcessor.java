package ru.etu.controlservice.service.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.etu.controlservice.dto.task.ResultPlanningPayload;
import ru.etu.controlservice.entity.AlignmentSegmentation;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.Task;
import ru.etu.controlservice.repository.NodeRepository;
import ru.etu.controlservice.service.ResultPlanningClient;
import ru.etu.controlservice.service.SegmentationNodeUpdater;
import ru.etu.grpc.segmentation.AnatomicalStructure;

import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResultPlanningProcessor implements TaskProcessor {
    private final ResultPlanningClient resultPlanningClient;
    private final SegmentationNodeUpdater segmentationNodeUpdater;
    private final NodeRepository nodeRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void process(Task task) {
        try {
            ResultPlanningPayload payload = objectMapper.readValue(task.getPayload(), ResultPlanningPayload.class);
            Long alignmentNodeId = payload.alignmentNodeId();

            Node resultPlanningNode = task.getNode();
            if (resultPlanningNode == null) {
                throw new IllegalStateException("No Node associated with task " + task.getId());
            }

            Node alignmentSegmentationNode =
                    nodeRepository.findByIdWithAlignmentSegmentation(alignmentNodeId);

            if (alignmentSegmentationNode == null) {
                throw new IllegalStateException("Required alignment segmentation not found");
            }

            AlignmentSegmentation alignmentSegmentation = alignmentSegmentationNode.getAlignmentSegmentation();
            List<String> stls = alignmentSegmentation.getStlToothRefs();
            List<String> initTeethMatrices = alignmentSegmentation.getInitTeethMatrices();

            List<AnatomicalStructure> anatomicalStructures = IntStream.range(0, stls.size())
                    .mapToObj(i -> AnatomicalStructure.newBuilder()
                            .setStl(stls.get(i))
                            .setInitMatrix(initTeethMatrices.get(i))
                            .build())
                    .toList();

            List<String> desiredTeethMatrices = resultPlanningClient.planResult(anatomicalStructures);
            if (desiredTeethMatrices == null) {
                log.error("ResultPlanning returned null for task {}", task.getId());
                throw new RuntimeException("ResultPlanning returned null result");
            }

            segmentationNodeUpdater.setResultPlanning(resultPlanningNode, alignmentSegmentation, desiredTeethMatrices);
        } catch (Exception e) {
            log.error("ResultPlanning failed for task {}: {}", task.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process ResultPlanning task", e);
        }
    }

    @Override
    public NodeType getSupportedType() {
        return NodeType.RESULT_PLANNING;
    }
}
