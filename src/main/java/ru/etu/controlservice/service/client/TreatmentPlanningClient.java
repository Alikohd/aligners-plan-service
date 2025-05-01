package ru.etu.controlservice.service.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.grpc.TreatmentPlanningGrpcDto;
import ru.etu.grpc.treatmentplanning.FinalAnatomicalStructure;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningRequest;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningResponse;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningServiceGrpc.TreatmentPlanningServiceBlockingStub;

import java.util.List;

@Service
@Slf4j
public class TreatmentPlanningClient {

    @GrpcClient("treatmentPlanningService")
    private TreatmentPlanningServiceBlockingStub stub;

    public TreatmentPlanningGrpcDto planTreatment(List<FinalAnatomicalStructure> structures) {
        TreatmentPlanningRequest request = TreatmentPlanningRequest.newBuilder().addAllStructures(structures).build();

        try {
            TreatmentPlanningResponse response = stub.planTreatment(request);
            return new TreatmentPlanningGrpcDto(response.getCollectionsOfMatricesGroupsList(), response.getAttachmentsList());
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            String description = e.getStatus().getDescription();
            log.error("gRPC call TreatmentPlanningService.planTreatment failed with status: {}, description: {}", code, description);
            throw e;
        }
    }
}
