package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request DTO for amending jaw segmentation results")
public record JawAmendRequestDto(
        @Schema(
                description = "List of amended jaw segmentation results, each specifying a tooth's ID, rotation angle, and translation vector",
                example = """
                        [
                            {
                                "tooth": "11",
                                "rotation": 15.5,
                                "translation": [0.5, 0.2, -0.3]
                            },
                            {
                                "tooth": "23",
                                "rotation": -10.0,
                                "translation": [-0.1, 0.4, 0.1]
                            },
                            {
                                "tooth": "31",
                                "rotation": 8.2,
                                "translation": [0.0, -0.3, 0.2]
                            }
                        ]
                        """
        )
        List<JsonNode> amendedJawsSegmented) {
}