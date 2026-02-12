package com.jay.voyager.web.mvc.controller.v1;

import java.time.Instant;
import java.util.UUID;

import com.jay.voyager.api.v1.jobs.VoyagerJobApi;
import com.jay.voyager.core.error.api.ApiException;
import com.jay.voyager.core.error.api.ErrorType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.jay.voyager.api.v1.jobs.model.VoyagerJobResponse;
import com.jay.voyager.core.context.identity.Identity;
import com.jay.voyager.core.context.identity.IdentityContextHolder;

@RestController
public class VoyagerJobController implements VoyagerJobApi {

    static final String SUCCESS_MESSAGE = "Voyager Endpoint Success.";

    @Override
    @GetMapping("/api/v1/jobs/{jobId}")
    public VoyagerJobResponse get(@PathVariable String jobId) {

        Identity identity = IdentityContextHolder.context().identity();

        UUID jobUuid;

        try {
             jobUuid = UUID.fromString(jobId);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(
                    ErrorType.BAD_REQUEST,
                    "Invalid Job Id. Must be UUID",
                    ex
            );
        }


        var job = new VoyagerJobResponse.Job(
                jobUuid,
                VoyagerJobResponse.JobStatus.SUCCEEDED,
                Instant.now()
        );

        return new VoyagerJobResponse(job, identity.requestId());
    }
}
