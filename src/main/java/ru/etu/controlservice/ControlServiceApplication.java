package ru.etu.controlservice;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import ru.etu.controlservice.service.ResultPlanningClient;
import ru.etu.grpc.resultplanning.AnatomicalStructure;

import java.util.List;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ControlServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ControlServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(ResultPlanningClient client) {
        return args -> {
            // Тестовые данные
            AnatomicalStructure structure = AnatomicalStructure.newBuilder()
                    .setStl("s3://test.stl")
                    .setRotationMatrix("[[1,0,0],[0,1,0],[0,0,1]]")
                    .build();

            // Используем бин
            System.out.println("Sending request...");
            List<String> result = client.planResult(List.of(structure));
            System.out.println("Result: " + result);
        };
    }
}
