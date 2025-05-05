package ru.etu.controlservice.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.persistence.EntityManager;
import lombok.SneakyThrows;
import org.awaitility.Awaitility;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.etu.controlservice.dto.task.AlignmentPayload;
import ru.etu.controlservice.dto.task.ResultPlanningPayload;
import ru.etu.controlservice.dto.task.SegmentationCtPayload;
import ru.etu.controlservice.dto.task.SegmentationJawPayload;
import ru.etu.controlservice.dto.task.TreatmentPlanningPayload;
import ru.etu.controlservice.entity.Node;
import ru.etu.controlservice.entity.NodeType;
import ru.etu.controlservice.entity.TreatmentCase;
import ru.etu.controlservice.entity.TreatmentPlanning;
import ru.etu.controlservice.grpc.FakeResultPlanningService;
import ru.etu.controlservice.grpc.FakeSegmentationService;
import ru.etu.controlservice.grpc.FakeTreatmentPlanningService;
import ru.etu.controlservice.service.NodeService;
import ru.etu.controlservice.service.PatientService;
import ru.etu.controlservice.service.TaskService;
import ru.etu.controlservice.service.TreatmentCaseService;
import ru.etu.controlservice.util.NodeTestUtils;
import ru.etu.controlservice.util.SegmentationTestData;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class QueueHandlerIT extends TestContainersConfig {

    @Value("${task-poller.backoff}")
    private long backoff;

    @Value("${task-poller.max-attempts}")
    private long maxAttempts;

    @Autowired
    private TaskService taskService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TreatmentCaseService treatmentCaseService;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager em;

    private static Server segmentationServer;
    private static Server resultPlanningServer;
    private static Server planningTreatmentServer;
    private static int segmentationPort;
    private static int planningResultPort;
    private static int planningTreatmentPort;

    @BeforeAll
    static void startServers() throws IOException {
        segmentationServer = ServerBuilder.forPort(0)
                .addService(new FakeSegmentationService())
                .build()
                .start();
        segmentationPort = segmentationServer.getPort();

        resultPlanningServer = ServerBuilder.forPort(0)
                .addService(new FakeResultPlanningService())
                .build()
                .start();
        planningResultPort = resultPlanningServer.getPort();

        planningTreatmentServer = ServerBuilder.forPort(0)
                .addService(new FakeTreatmentPlanningService())
                .build()
                .start();
        planningTreatmentPort = planningTreatmentServer.getPort();
    }

    @DynamicPropertySource
    static void overrideGrpcClientProps(DynamicPropertyRegistry registry) {
        registry.add("grpc.client.segmentationService.address", () -> "static://localhost:" + segmentationPort);
        registry.add("grpc.client.resultPlanningService.address", () -> "static://localhost:" + planningResultPort);
        registry.add("grpc.client.treatmentPlanningService.address", () -> "static://localhost:" + planningTreatmentPort);
    }

    @AfterAll
    static void stopGrpcServer() throws InterruptedException {
        segmentationServer.shutdownNow().awaitTermination();
        resultPlanningServer.shutdownNow().awaitTermination();
        planningTreatmentServer.shutdownNow().awaitTermination();
    }

    @AfterEach
    void truncateAllTables() {
        List<String> tableNames = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                        "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' " +
                        "AND table_name NOT IN ('databasechangelog', 'databasechangeloglock')",
                String.class
        );

        jdbcTemplate.execute("SET session_replication_role = 'replica';");
        for (String table : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table + " CASCADE;");
        }
        jdbcTemplate.execute("SET session_replication_role = 'origin';");

    }

    @Test
    public void queueHandler_ShouldProcessCtTask_WhenPayloadValid() {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        SegmentationCtPayload payload = new SegmentationCtPayload(SegmentationTestData.mockCtOriginalUri);
        String expectedCtMaskUri = SegmentationTestData.mockCtMaskUri;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        Node ctNode = template.execute((status) -> {
            try {
                Node node = nodeService.addStepToEnd(treatmentCase);
                taskService.addTask(mapper.writeValueAsString(payload), NodeType.SEGMENTATION_CT, node);
                return node;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(1, countMessagesInQueue("tasks"));
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> countMessagesInQueue("tasks") == 0);
        String actualMaskUri = template.execute((status) -> {
            assert ctNode != null;
            return nodeService.getNode(ctNode.getId()).getCtSegmentation().getCtMask().getUri();
        });
        assertEquals(expectedCtMaskUri, actualMaskUri);
    }

    @Test
    public void queueHandler_ShouldProcessJawTask_WhenPayloadValid() throws JsonProcessingException {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        SegmentationJawPayload payload = new SegmentationJawPayload(SegmentationTestData.mockJawUpperUri, SegmentationTestData.mockJawLowerUri);
        String expectedJawsSegmentedString = SegmentationTestData.mockJawsSegmented;
        JsonNode jsonArray = mapper.readTree(expectedJawsSegmentedString);
        List<JsonNode> expectedJawsSegmented = new java.util.ArrayList<>();
        jsonArray.elements().forEachRemaining(expectedJawsSegmented::add);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        Node jawNode = template.execute((status) -> {
            try {
                Node node = nodeService.addStepToEnd(treatmentCase);
                taskService.addTask(mapper.writeValueAsString(payload), NodeType.SEGMENTATION_JAW, node);
                return node;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(1, countMessagesInQueue("tasks"));
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> countMessagesInQueue("tasks") == 0);
        List<JsonNode> actualJawsSegmented = template.execute((status) -> {
            assert jawNode != null;
            return nodeService.getNode(jawNode.getId()).getJawSegmentation().getJawsSegmented();
        });
        assertEquals(expectedJawsSegmented, actualJawsSegmented);
    }

    @Test
    public void queueHandler_ShouldProcessAlignmentTask_WhenPayloadValid() throws JsonProcessingException {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        String expectedInitTeethMatricesString = SegmentationTestData.mockInitTeethMatrices;
        JsonNode jsonArray = mapper.readTree(expectedInitTeethMatricesString);
        List<JsonNode> expectedInitTeethMatrices = new java.util.ArrayList<>();
        jsonArray.elements().forEachRemaining(expectedInitTeethMatrices::add);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        AlignmentPayload alignmentPayload = template.execute((status) -> {
            UUID ctNodeId = NodeTestUtils.createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
            UUID jawNodeId = NodeTestUtils.createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
            return new AlignmentPayload(ctNodeId, jawNodeId);
        });

        Node alignmentNode = template.execute((status) -> {
            Node node = nodeService.addStepToEnd(treatmentCase);
            try {
                taskService.addTask(mapper.writeValueAsString(alignmentPayload), NodeType.SEGMENTATION_ALIGNMENT, node);
                return node;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(1, countMessagesInQueue("tasks"));
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> countMessagesInQueue("tasks") == 0);
        List<JsonNode> actualInitTeethMatrices = template.execute((status) -> {
            assert alignmentNode != null;
            return nodeService.getNode(alignmentNode.getId()).getAlignmentSegmentation().getInitTeethMatrices();
        });
        assertEquals(expectedInitTeethMatrices, actualInitTeethMatrices);
    }

    @Test
    public void queueHandler_ShouldProcessResultTask_WhenPayloadValid() {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        List<JsonNode> expectedDesiredTeethMatrices = SegmentationTestData.getJsonNodes(SegmentationTestData.mockDesiredTeethMatrices);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        UUID alignmentId = template.execute((status) -> {
            NodeTestUtils.createCtSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
            NodeTestUtils.createJawSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
            return NodeTestUtils.createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
        });

        ResultPlanningPayload resultPlanningPayload = new ResultPlanningPayload(alignmentId);
        Node resultNode = template.execute((status) -> {
            Node node = nodeService.addStepToEnd(treatmentCase);
            try {
                taskService.addTask(mapper.writeValueAsString(resultPlanningPayload), NodeType.RESULT_PLANNING, node);
                return node;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(1, countMessagesInQueue("tasks"));
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> countMessagesInQueue("tasks") == 0);
        List<JsonNode> actualDesiredTeethMatrices = template.execute((status) -> {
            assert resultNode != null;
            return nodeService.getNode(resultNode.getId()).getResultPlanning().getDesiredTeethMatrices();
        });
        assertEquals(expectedDesiredTeethMatrices, actualDesiredTeethMatrices);
    }

    @Test
    public void queueHandler_ShouldProcessTreatmentTask_WhenPayloadValid() {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        JsonNode expectedTreatmentStepMatrixGroup = SegmentationTestData.getJsonNode(SegmentationTestData.mockMatrixGroup);
        JsonNode expectedAttachment = SegmentationTestData.getJsonNode(SegmentationTestData.mockAttachment);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult((status) -> {
            NodeTestUtils.createAlignmentSegmentationNode(patientId, caseId, treatmentCaseService, nodeService);
            UUID resultPlanningId = NodeTestUtils.createResultPlanningNode(patientId, caseId, treatmentCaseService, nodeService);
            Node node = nodeService.getNode(resultPlanningId);
            TreatmentPlanningPayload treatmentPlanningPayload = new TreatmentPlanningPayload(resultPlanningId);
            try {
                taskService.addTask(mapper.writeValueAsString(treatmentPlanningPayload), NodeType.TREATMENT_PLANNING, node);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        assertEquals(1, countMessagesInQueue("tasks"));
        Awaitility.await()
                .atMost(Duration.ofSeconds(4))
                .pollInterval(Duration.ofMillis(500))
                .until(() -> countMessagesInQueue("tasks") == 0);
        TreatmentPlanning actualResponse = template.execute((status -> {
            Node freshRoot = nodeService.getNode(treatmentCase.getRoot().getId());
            TreatmentPlanning treatmentPlanning = nodeService.findLastNode(freshRoot).getTreatmentPlanning();
            Hibernate.initialize(treatmentPlanning);
            return treatmentPlanning;
        }));
        assert actualResponse != null;
        JsonNode actualTreatmentStepMatrixGroup = actualResponse.getTreatmentStepMatrixGroup();
        JsonNode actualAttachment = actualResponse.getAttachment();

        assertEquals(expectedTreatmentStepMatrixGroup, actualTreatmentStepMatrixGroup);
        assertEquals(expectedAttachment, actualAttachment);
    }

    @Test
    @SneakyThrows
    public void queueHandler_ShouldSendTaskToDql_WhenPayloadInvalid() {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        String invalidPayload = "some plain string";
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult((status) -> {
            Node node = nodeService.addStepToEnd(treatmentCase);
            try {
                taskService.addTask(mapper.writeValueAsString(invalidPayload), NodeType.SEGMENTATION_CT, node);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        Awaitility.await()
                .atMost(Duration.ofMillis((long) (backoff * maxAttempts * 1.1)))
                .pollInterval(Duration.ofMillis(backoff / 3))
                .until(() -> countMessagesInQueue("dlq") == 1);
    }

    @Test
    @SneakyThrows
    public void queueHandler_ShouldSendTaskToDql_WhenNodeTypeInvalid() {
        UUID patientId = patientService.addPatient().id();
        UUID caseId = treatmentCaseService.createCase(patientId).id();
        TreatmentCase treatmentCase = treatmentCaseService.getCaseById(patientId, caseId);
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        SegmentationCtPayload payload = new SegmentationCtPayload(SegmentationTestData.mockCtOriginalUri);
        template.executeWithoutResult((status) -> {
            Node node = nodeService.addStepToEnd(treatmentCase);
            try {
                taskService.addTask(mapper.writeValueAsString(payload), NodeType.EMPTY_NODE, node);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        Awaitility.await()
                .atMost(Duration.ofMillis((long) (backoff * maxAttempts * 1.1)))
                .pollInterval(Duration.ofMillis(backoff / 3))
                .until(() -> countMessagesInQueue("dlq") == 1);
    }

    private int countMessagesInQueue(String region) {
        Object result = em.createNativeQuery("""
                        SELECT COUNT(*) FROM int_channel_message
                        WHERE region = :region
                        """)
                .setParameter("region", region)
                .getSingleResult();
        return ((Number) result).intValue();
    }

}
