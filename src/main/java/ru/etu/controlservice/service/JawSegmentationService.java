package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.JawSegmentationGrpc;
import ru.etu.controlservice.JawSegmentationServiceProto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JawSegmentationService {

    @GrpcClient("jawSegmentation")
    private JawSegmentationGrpc.JawSegmentationBlockingStub stub;

    public List<String> sendToPlug(String filePathUpperStl, String filePathLowerStl){
        JawSegmentationServiceProto.StlRequest request = JawSegmentationServiceProto.StlRequest.newBuilder()
                .setFilePathLowerStl(filePathLowerStl)
                .setFilePathUpperStl(filePathUpperStl)
                .build();

        JawSegmentationServiceProto.JsonReply reply = stub.sendStlUrl(request);

        return reply.getJsonList();

    }

}
