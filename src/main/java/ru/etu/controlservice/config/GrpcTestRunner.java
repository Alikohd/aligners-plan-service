package ru.etu.controlservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.etu.controlservice.AlignmentSegmentationServiceProto;
import ru.etu.controlservice.dto.grpc.TreatmentPlanningDto;
import ru.etu.controlservice.service.AlignmentSegmentationService;
import ru.etu.controlservice.service.JawSegmentationService;
import ru.etu.controlservice.service.PacsService;
import ru.etu.controlservice.service.ResultPlanningClient;
import ru.etu.controlservice.service.TreatmentPlanningClient;
import ru.etu.grpc.resultplanning.AnatomicalStructure;
import ru.etu.grpc.treatmentplanning.FinalAnatomicalStructure;

import java.util.List;

@Configuration
@Slf4j
public class GrpcTestRunner {

    @Bean
    public CommandLineRunner run(ResultPlanningClient planningResClient,
                                 TreatmentPlanningClient planningTreatClient,
                                 PacsService pacsService,
                                 JawSegmentationService jawSegmentationService,
                                 AlignmentSegmentationService alignmentSegmentationService) {
        return args -> {
            // Тестовые данные
            log.info("Sending pacs request");
            String pacsResponse = pacsService.sendToPlug("testSeriesId");
            log.info("Result: " + pacsResponse);

            log.info("Sending jaw request");
            List<String> jawResponse = jawSegmentationService.sendToPlug("s3://test.stl", "s3://test.stl");
            log.info("Result: " + jawResponse);

            log.info("Sending alignmentSeg request");
            List<AlignmentSegmentationServiceProto.AnatomicalStructure> segResponse = alignmentSegmentationService.sendToPlug("testSeriesId", "s3://test.stl", "s3://test.stl", List.of("{sample_text: json}"));
            log.info("Result: " + segResponse);

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
