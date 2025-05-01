package ru.etu.controlservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request DTO for amending treatment planning step matrices and attachment")
public record TreatmentPlanningAmendRequest(
        @Schema(
                description = "Group of transformation matrices for treatment planning steps, containing a list of 4x4 matrices",
                example = """
                        {
                            "matrices": [
                                [
                                    [0.8521, 0.2285, 0.1355, 0.3302],
                                    [0.1712, 0.7458, 0.2945, 0.4046],
                                    [0.3323, 0.1742, 0.9850, 0.2051],
                                    [0.0000, 0.0313, 0.000, 1.0052]
                                ],
                                [
                                    [0.6596, 0.3584, 0.2894, 0.4625],
                                    [0.2446, 0.8812, 0.1557, 0.5543],
                                    [0.4232, 0.2758, 0.8340, 0.3571],
                                    [0.0309, 0.0000, 0.0622, 1.0000]
                                ],
                                [
                                    [0.7565, 0.1828, 0.3932, 0.2345],
                                    [0.3012, 0.8234, 0.2056, 0.6320],
                                    [0.2588, 0.3495, 0.8538, 0.1523],
                                    [0.0000, 0.0000, 0.0000, 1.0000]
                                ]
                            ]
                        }
                        """
        )
        JsonNode treatmentStepMatrixGroup,

        @Schema(
                description = "Attachment details including type and 3D coordinates",
                example = """
                        {
                            "type": "attachment_1",
                            "coordinates": {
                                "x": 1.5521,
                                "y": -0.8201,
                                "z": 0.3453
                            }
                        }
                        """
        )
        JsonNode attachment
) {
}