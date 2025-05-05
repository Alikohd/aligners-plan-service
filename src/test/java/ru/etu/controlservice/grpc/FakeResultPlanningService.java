package ru.etu.controlservice.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import ru.etu.controlservice.util.SegmentationTestData;
import ru.etu.grpc.resultplanning.ResultPlanningRequest;
import ru.etu.grpc.resultplanning.ResultPlanningResponse;
import ru.etu.grpc.resultplanning.ResultPlanningServiceGrpc;

import java.util.ArrayList;
import java.util.List;

public class FakeResultPlanningService extends ResultPlanningServiceGrpc.ResultPlanningServiceImplBase {

    @Override
    @SneakyThrows
    public void planResult(ResultPlanningRequest request, StreamObserver<ResultPlanningResponse> responseObserver) {
        String mockInitMatrix = SegmentationTestData.mockDesiredTeethMatrices;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockInitMatrix);
        List<Struct> desiredMatrixStructs = new ArrayList<>();
        for (JsonNode element : jsonNode) {
            Struct.Builder structBuilder = Struct.newBuilder();
            JsonFormat.parser().merge(element.toString(), structBuilder);
            desiredMatrixStructs.add(structBuilder.build());
        }
        ResultPlanningResponse reply = ResultPlanningResponse.newBuilder()
                .addAllDesiredTeethMatrices(desiredMatrixStructs)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
