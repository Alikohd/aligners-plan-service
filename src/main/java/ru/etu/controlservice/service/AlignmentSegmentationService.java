package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.AlignmentSegmentationGrpc;
import ru.etu.controlservice.JawSegmentationGrpc;
import ru.etu.controlservice.JawSegmentationServiceProto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlignmentSegmentationService {

    @GrpcClient("AlignmentSegmentationClient")
    private AlignmentSegmentationGrpc.AlignmentSegmentationBlockingStub stub;

    private List<String> sendToPlug(String filePathUpperStl, String filePathLowerStl){
        JawSegmentationServiceProto.StlRequest request = JawSegmentationServiceProto.StlRequest.newBuilder()
                .setFilePathLowerStl(filePathLowerStl)
                .setFilePathUpperStl(filePathUpperStl)
                .build();

        JawSegmentationServiceProto.JsonReply reply = stub.sendStlUrl(request);

        return reply.getJsonList();

    }

}
