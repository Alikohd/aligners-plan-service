package ru.etu.controlservice.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import ru.etu.controlservice.dto.grpc.TreatmentPlanningDto;
import ru.etu.grpc.treatmentplanning.FinalAnatomicalStructure;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningRequest;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningResponse;
import ru.etu.grpc.treatmentplanning.TreatmentPlanningServiceGrpc.TreatmentPlanningServiceBlockingStub;

import java.util.List;

@Service
public class TreatmentPlanningClient {

    @GrpcClient("treatmentPlanningService")
    private TreatmentPlanningServiceBlockingStub stub;

    public TreatmentPlanningDto planTreatment(List<FinalAnatomicalStructure> structures) {
        TreatmentPlanningRequest request = TreatmentPlanningRequest.newBuilder().addAllStructures(structures).build();
        TreatmentPlanningResponse response = stub.planTreatment(request);

        return new TreatmentPlanningDto(response.getCollectionsOfMatricesGroupsList(), response.getAttachmentsList());
    }

}
