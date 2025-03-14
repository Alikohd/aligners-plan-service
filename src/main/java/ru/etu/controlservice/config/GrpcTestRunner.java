package ru.etu.controlservice.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.etu.controlservice.dto.grpc.TreatmentPlanningDto;
import ru.etu.controlservice.service.ResultPlanningClient;
import ru.etu.controlservice.service.TreatmentPlanningClient;
import ru.etu.grpc.resultplanning.AnatomicalStructure;
import ru.etu.grpc.treatmentplanning.FinalAnatomicalStructure;

import java.util.List;

@Configuration
public class GrpcTestRunner {

    @Bean
    public CommandLineRunner run(ResultPlanningClient planningResClient, TreatmentPlanningClient planningTreatClient) {
        return args -> {
            // Тестовые данные
            AnatomicalStructure structure = AnatomicalStructure.newBuilder()
                    .setStl("s3://test.stl")
                    .setInitMatrix("[[1,0,0],[0,1,0],[0,0,1],[0,0,1]]")
                    .build();

            System.out.println("Sending request...");
            List<String> resultPlanning = planningResClient.planResult(List.of(structure));
            System.out.println("Result: " + resultPlanning.get(9));


            FinalAnatomicalStructure finalStructure = FinalAnatomicalStructure.newBuilder()
                    .setStl("s3://test.stl")
                    .setInitMatrix("[[1,0,0],[0,1,0],[0,0,1],[0,0,1]]")
                    .setInitMatrix("[[1,0,1],[0,1,1],[0,0,1],[1,1,1]]")
                    .build();

            System.out.println("Sending request...");
            TreatmentPlanningDto treatDto = planningTreatClient.planResult(List.of(finalStructure));
            System.out.println("Result: " + treatDto.collectionsOfMatricesGroups().get(5));
        };
    }
}
