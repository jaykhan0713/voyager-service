package com.jay.voyager.api.v1.jobs.model;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "VoyagerJobResponse",
        description = "Voyager job data returned by the GET /api/v1/jobs/{jobId} endpoint."
)
public record VoyagerJobResponse(

        Job job,

        @Schema(
                description = "Request id from the incoming identity header. Empty when not provided.",
                example = "a1a7c9a73c4bdcb9acf3175c41371da0"
        )
        String requestId
) {
    @Schema(name = "Job", description = "Job entity returned by Voyager.")
    public record Job(
            @Schema(description = "Job id")
            UUID id,

            @Schema(description = "Current job status")
            JobStatus status,

            @Schema(description = "Requested at time")
            Instant requestedAt
    ) {}

    public enum JobStatus {
        QUEUED,
        RUNNING,
        SUCCEEDED,
        FAILED
    }
}
