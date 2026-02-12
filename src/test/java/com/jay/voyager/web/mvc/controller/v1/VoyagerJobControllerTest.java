package com.jay.voyager.web.mvc.controller.v1;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jay.voyager.api.v1.jobs.model.VoyagerJobResponse;
import com.jay.voyager.core.context.identity.Identity;
import com.jay.voyager.core.context.identity.IdentityContextHolder;
import com.jay.voyager.core.context.identity.IdentityContextSnapshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoyagerJobControllerTest {

    @BeforeEach
    void setUp() {
        IdentityContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        IdentityContextHolder.clear();
    }

    @Test
    void getReturnsResponse() {
        String userId = "user-001";
        String requestId = "identity-001";

        Identity identity = new Identity(userId, requestId);
        IdentityContextHolder.context(IdentityContextSnapshot.of(identity));

        VoyagerJobController controller = new VoyagerJobController();

        UUID uuid = UUID.randomUUID();
        Instant beforeRequestedAt = Instant.now();

        VoyagerJobResponse voyagerResponse = controller.get(uuid.toString());

        var responseJob = voyagerResponse.job();

        assertEquals(uuid, responseJob.id());
        assertEquals(VoyagerJobResponse.JobStatus.SUCCEEDED, responseJob.status());
        assertTrue(beforeRequestedAt.toEpochMilli() <= responseJob.requestedAt().toEpochMilli());

        assertEquals(requestId, voyagerResponse.requestId());
    }
}