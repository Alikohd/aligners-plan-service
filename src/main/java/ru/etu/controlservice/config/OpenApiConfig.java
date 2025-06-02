package ru.etu.controlservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Treatment plan management API")
                        .version("1.0.0")
                        .description("""
                                Микросервис для моделирования и управления планом лечения в ортодонтии
                         
                                [Примеры входных данных](https://disk.yandex.ru/d/yzX37EGlXTkxWw)
                                """))
                .tags(List.of(
                        new Tag().name("Patient").description("API для управления пациентами"),
                        new Tag().name("Treatment Case").description("Работа со случаями лечения (case)"),
                        new Tag().name("CT Segmentation").description("Работа с сегментацией КТ"),
                        new Tag().name("Jaw Segmentation").description("Работа с сегментацией челюстей"),
                        new Tag().name("Alignment").description("Работа с совмещением сегментаций КТ и челюстей"),
                        new Tag().name("Result Planning").description("Работа с планированием результата"),
                        new Tag().name("Treatment Planning").description("Работа с планированием лечения и шагами лечения"),
                        new Tag().name("Node Content").description("Операции для получения содержимого узлов"),
                        new Tag().name("File Management").description("Операции для работы с файлами")
                ));
    }

    @Bean
    public OpenApiCustomizer sortTags() {
        return openApi -> {
            List<Tag> tags = openApi.getTags();
            if (tags != null) {
                List<String> desiredOrder = List.of("Patient", "Treatment Case", "CT Segmentation", "Jaw Segmentation", "Alignment", "Result Planning", "Treatment Planning");
                tags.sort(Comparator.comparingInt(tag -> {
                    int index = desiredOrder.indexOf(tag.getName());
                    return index >= 0 ? index : Integer.MAX_VALUE;
                }));
            }
        };
    }

}