package ru.etu.controlservice.service;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.AlignmentSegmentationGrpc;
import ru.etu.controlservice.AlignmentSegmentationServiceProto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlignmentSegmentationService {

    @Value("${pacs.address.base}")
    private String pacsBase;

    @GrpcClient("alignmentSegmentation")
    private AlignmentSegmentationGrpc.AlignmentSegmentationBlockingStub stub;

    public List<AlignmentSegmentationServiceProto.AnatomicalStructure> sendToPlug(String seriesId,
                                                                                   String filePathUpperStl,
                                                                                   String filePathLowerStl,
                                                                                   List<String> jsons){
        AlignmentSegmentationServiceProto.CtJawRequest request = AlignmentSegmentationServiceProto.CtJawRequest.newBuilder()
                .setPacsBase(pacsBase)
                .setSeriesId(seriesId)
                .setFilePathUpperStl(filePathUpperStl)
                .setFilePathLowerStl(filePathLowerStl)
                .addAllJson(jsons)
                .build();

        AlignmentSegmentationServiceProto.CombinationReply reply = stub.sendCombining(request);

        return reply.getStructuresList();
    }

}
