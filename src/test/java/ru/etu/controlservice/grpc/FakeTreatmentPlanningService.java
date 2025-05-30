package ru.etu.controlservice.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import ru.etu.controlservice.util.SegmentationTestData;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningRequest;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningResponse;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningServiceGrpc;

public class FakeTreatmentPlanningService extends TreatmentPlanningServiceGrpc.TreatmentPlanningServiceImplBase {

    @Override
    @SneakyThrows
    public void planTreatment(TreatmentPlanningRequest request, StreamObserver<TreatmentPlanningResponse> responseObserver) {
        // Получаем моковые данные
        String mockMatrixGroup = SegmentationTestData.mockMatrixGroup;
        String mockAttachment = SegmentationTestData.mockAttachment;

        // Инициализируем ObjectMapper для парсинга JSON
        ObjectMapper mapper = new ObjectMapper();

        // Парсим mockMatrixGroup как один JSON-объект
        JsonNode jsonMatrix = mapper.readTree(mockMatrixGroup);
        Struct.Builder matrixGroupBuilder = Struct.newBuilder();
        JsonFormat.parser().merge(jsonMatrix.toString(), matrixGroupBuilder);
        Struct matrixGroupStruct = matrixGroupBuilder.build();

        // Парсим mockAttachment как один JSON-объект
        JsonNode jsonAttachment = mapper.readTree(mockAttachment);
        Struct.Builder attachmentBuilder = Struct.newBuilder();
        JsonFormat.parser().merge(jsonAttachment.toString(), attachmentBuilder);
        Struct attachmentStruct = attachmentBuilder.build();

        // Формируем ответ
        TreatmentPlanningResponse reply = TreatmentPlanningResponse.newBuilder()
                .addCollectionsOfMatricesGroups(matrixGroupStruct) // Добавляем группу матриц
                .addAttachments(attachmentStruct)                  // Добавляем вложение
                .build();

        // Отправляем ответ клиенту
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}