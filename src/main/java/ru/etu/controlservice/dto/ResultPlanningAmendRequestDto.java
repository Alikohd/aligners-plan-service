package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request DTO for amending desired teeth transformation matrices")
public record ResultPlanningAmendRequestDto(
        @Schema(
                description = "List of amended desired transformation matrices for teeth, each containing a 3x4 matrix",
                example = """
                        [
                            {
                                "matrix": [
                                    [0.7, 0.3, 0.1, 0.2],
                                    [0.2, 0.8, 0.4, 0.5],
                                    [0.0, 0.0, 0.0, 1.0]
                                ]
                            },
                            {
                                "matrix": [
                                    [0.6, 0.2, 0.3, 0.4],
                                    [0.3, 0.9, 0.2, 0.6],
                                    [0.0, 0.0, 0.0, 1.0]
                                ]
                            },
                            {
                                "matrix": [
                                    [0.8, 0.1, 0.2, 0.3],
                                    [0.4, 0.7, 0.3, 0.5],
                                    [0.0, 0.0, 0.0, 1.0]
                                ]
                            },
                            {
                                "matrix": [
                                    [0.5, 0.4, 0.3, 0.2],
                                    [0.3, 0.6, 0.5, 0.4],
                                    [0.0, 0.0, 0.0, 1.0]
                                ]
                            }
                        ]
                        """
        )
        List<JsonNode> amendedDesiredTeethMatrices) {
}