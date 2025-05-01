package ru.etu.controlservice.service.client;

import com.google.protobuf.Struct;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.grpc.resultplanning.ResultPlanningRequest;
import ru.etu.grpc.resultplanning.ResultPlanningResponse;
import ru.etu.grpc.resultplanning.ResultPlanningServiceGrpc.ResultPlanningServiceBlockingStub;
import ru.etu.grpc.segmentation.AnatomicalStructure;

import java.util.List;

@Service
@Slf4j
public class ResultPlanningClient {

    @GrpcClient("resultPlanningService")
    private ResultPlanningServiceBlockingStub stub;

    public List<Struct> planResult(List<AnatomicalStructure> structures) {
        ResultPlanningRequest request = ResultPlanningRequest.newBuilder().addAllStructures(structures).build();
        try {
            ResultPlanningResponse response = stub.planResult(request);
            return response.getDesiredTeethMatricesList();
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            String description = e.getStatus().getDescription();
            log.error("gRPC call ResultPlanningService.planResult failed with status: {}, description: {}", code, description);
            throw e;
        }
    }
}
