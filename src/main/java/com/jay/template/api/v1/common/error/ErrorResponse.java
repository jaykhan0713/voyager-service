package com.jay.template.api.v1.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ErrorResponse",
        description = "Standard error response returned when a identity cannot be processed."
)
public record ErrorResponse(

        @Schema(
                description = "Stable application-specific error code (varies by failure type).",
                example = "BAD_REQUEST"
        )
        String code,

        @Schema(
                description = "Human-readable error message describing the failure.",
                example = "Error Message."
        )
        String message,

        @Schema(
                description = "Correlation id for this identity. Provide this value when contacting support.",
                example = "a1a7c9a73c4bdcb9acf3175c41371da0"
        )
        String correlationId
) {}
