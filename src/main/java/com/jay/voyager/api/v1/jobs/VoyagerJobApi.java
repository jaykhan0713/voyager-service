package com.jay.voyager.api.v1.jobs;

import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.jay.voyager.api.v1.common.error.openapi.StandardErrorResponses;
import com.jay.voyager.api.v1.jobs.model.VoyagerJobResponse;

@Tag(
        name = "VoyagerJobApi",
        description = "Endpoints used to demonstrate the jobs structure and conventions."
)
public interface VoyagerJobApi {

    @Operation(
            summary = "GET /api/v1/jobs/{jobId}",
            description = "Demonstrates API structure, identity extraction, and OpenAPI documentation."
    )
    @StandardErrorResponses
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Success",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VoyagerJobResponse.class)
                    )
            )
    })
    VoyagerJobResponse get(String jobId);
}