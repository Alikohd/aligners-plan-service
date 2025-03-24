package ru.etu.controlservice.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.grpc.resultplanning.ResultPlanningRequest;
import ru.etu.grpc.resultplanning.ResultPlanningResponse;
import ru.etu.grpc.resultplanning.ResultPlanningServiceGrpc.ResultPlanningServiceBlockingStub;
import ru.etu.grpc.segmentation.AnatomicalStructure;

import java.util.List;

@Service
public class ResultPlanningClient {

    @GrpcClient("resultPlanningService")
    private ResultPlanningServiceBlockingStub stub;

    public List<String> planResult(List<AnatomicalStructure> structures) {
        ResultPlanningRequest request = ResultPlanningRequest.newBuilder().addAllStructures(structures).build();

        ResultPlanningResponse response = stub.planResult(request);

        return response.getDesiredTeethMatricesList();
    }
}
