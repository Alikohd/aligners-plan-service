package ru.etu.controlservice.grpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import ru.etu.controlservice.util.SegmentationTestData;
import ru.etu.grpc.segmentation.AlignmentRequest;
import ru.etu.grpc.segmentation.AlignmentResponse;
import ru.etu.grpc.segmentation.AnatomicalStructure;
import ru.etu.grpc.segmentation.CtRequest;
import ru.etu.grpc.segmentation.CtResponse;
import ru.etu.grpc.segmentation.JawRequest;
import ru.etu.grpc.segmentation.JawResponse;
import ru.etu.grpc.segmentation.SegmentationServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static ru.etu.controlservice.util.ProtobufUtils.jsonNodesToStructs;

public class FakeSegmentationService extends SegmentationServiceGrpc.SegmentationServiceImplBase {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void segmentCt(CtRequest request, StreamObserver<CtResponse> responseObserver) {
        CtResponse response = CtResponse.newBuilder()
                .setCtMask(SegmentationTestData.mockCtMaskUri)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void segmentJaw(JawRequest request, StreamObserver<JawResponse> responseObserver) {
        String jsonString = SegmentationTestData.mockJawsSegmented;
        JawResponse response;
        try {
            JsonNode jsonArray = mapper.readTree(jsonString);
            List<JsonNode> jsonNodes = new java.util.ArrayList<>();
            jsonArray.elements().forEachRemaining(jsonNodes::add);
            List<Struct> structs = jsonNodesToStructs(jsonNodes);
            response = JawResponse.newBuilder()
                    .addAllJawsSegmented(structs)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mockJson", e);
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    @SneakyThrows
    public void align(AlignmentRequest request, StreamObserver<AlignmentResponse> responseObserver) {
        String mockInitMatrix = SegmentationTestData.mockInitTeethMatrices;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mockInitMatrix);
        List<Struct> initMatrixStructs = new ArrayList<>();
        for (JsonNode element : jsonNode) {
            Struct.Builder structBuilder = Struct.newBuilder();
            JsonFormat.parser().merge(element.toString(), structBuilder);
            initMatrixStructs.add(structBuilder.build());
        }

        List<AnatomicalStructure> structures = IntStream.range(0, Math.min(initMatrixStructs.size(), 10))
                .mapToObj(i -> AnatomicalStructure.newBuilder()
                        .setStl(SegmentationTestData.mockAlignmentStl + i)
                        .setInitMatrix(initMatrixStructs.get(i))
                        .build())
                .toList();

        AlignmentResponse reply = AlignmentResponse.newBuilder()
                .addAllInitStructures(structures)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
