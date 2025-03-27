package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
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

    public List<String> segmentJaw(String filePathUpperStl, String filePathLowerStl) {
        JawRequest request = JawRequest.newBuilder()
                .setJawLowerStl(filePathLowerStl)
                .setJawUpperStl(filePathUpperStl)
                .build();

        JawResponse response = stub.segmentJaw(request);
        return response.getJawsJsonList();
    }

    public List<AnatomicalStructure> align(String ctMask, String filePathUpperStl,
                                           String filePathLowerStl, List<String> jawsJson) {
        AlignmentRequest request = AlignmentRequest.newBuilder()
                .setCtMask(ctMask)
                .setJawUpperStl(filePathUpperStl)
                .setJawLowerStl(filePathLowerStl)
                .addAllJawsJson(jawsJson)
                .build();

        AlignmentResponse response = stub.align(request);

        return response.getInitStructuresList();
    }

}
