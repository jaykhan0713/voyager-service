package com.jay.template.api.v1.sample.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SampleResponse", description = "Basic sample data returned by the /v1/sample endpoint.")
public record SampleResponse(

        @Schema(
                description = "Human-readable message.",
                example = "Sample Endpoint Success"
        )
        String message,

        @Schema(
                description = "Request id from the incoming identity header. Empty when not provided.",
                example = "a1a7c9a73c4bdcb9acf3175c41371da0"
        )
        String requestId
) {}
