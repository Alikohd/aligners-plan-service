package ru.etu.controlservice.service.GrpcClient;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.grpc.TreatmentPlanningDto;
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

    public TreatmentPlanningDto planTreatment(List<FinalAnatomicalStructure> structures) {
        TreatmentPlanningRequest request = TreatmentPlanningRequest.newBuilder().addAllStructures(structures).build();

        try {
            TreatmentPlanningResponse response = stub.planTreatment(request);
            return new TreatmentPlanningDto(response.getCollectionsOfMatricesGroupsList(), response.getAttachmentsList());
        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            String description = e.getStatus().getDescription();
            log.error("gRPC call TreatmentPlanningService.planTreatment failed with status: {}, description: {}", code, description);
            throw e;
        }
    }
}
