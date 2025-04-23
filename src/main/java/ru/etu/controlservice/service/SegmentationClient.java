package ru.etu.controlservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.Struct;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.util.ProtobufUtils;
import ru.etu.grpc.segmentation.AlignmentRequest;
import ru.etu.grpc.segmentation.AlignmentResponse;
import ru.etu.grpc.segmentation.AnatomicalStructure;
import ru.etu.grpc.segmentation.CtRequest;
import ru.etu.grpc.segmentation.CtResponse;
import ru.etu.grpc.segmentation.JawRequest;
import ru.etu.grpc.segmentation.JawResponse;
import ru.etu.grpc.segmentation.SegmentationServiceGrpc.SegmentationServiceBlockingStub;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentationClient {

    @GrpcClient("segmentationService")
    private SegmentationServiceBlockingStub stub;

    public String segmentCt(String ctOriginal) {
        CtRequest request = CtRequest.newBuilder()
                .setCtOriginal(ctOriginal)
                .build();

        CtResponse response = stub.segmentCt(request);

        return response.getCtMask();
    }

    public List<Struct> segmentJaw(String filePathUpperStl, String filePathLowerStl) {
        JawRequest request = JawRequest.newBuilder()
                .setJawLower(filePathLowerStl)
                .setJawUpper(filePathUpperStl)
                .build();

        JawResponse response = stub.segmentJaw(request);
        return response.getJawsSegmentedList();
    }

    public List<AnatomicalStructure> align(String ctMask, String filePathUpperStl,
                                           String filePathLowerStl, List<JsonNode> jawsSegmented) {
        List<Struct> structs = ProtobufUtils.jsonNodesToStructs(jawsSegmented);
        AlignmentRequest request = AlignmentRequest.newBuilder()
                .setCtMask(ctMask)
                .setJawUpperStl(filePathUpperStl)
                .setJawLowerStl(filePathLowerStl)
                .addAllJawsSegmented(structs)
                .build();

        AlignmentResponse response = stub.align(request);

        return response.getInitStructuresList();
    }
}
