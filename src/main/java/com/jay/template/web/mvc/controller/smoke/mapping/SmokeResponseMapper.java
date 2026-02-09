package com.jay.template.web.mvc.controller.smoke.mapping;

import org.springframework.stereotype.Component;

import com.jay.template.core.domain.smoke.SmokeCheckResult;
import com.jay.template.web.mvc.controller.smoke.api.model.SmokeResponse;

// Translate app domain result into API response contract
@Component
public class SmokeResponseMapper {

    public SmokeResponse mapSmokeCheckResult(SmokeCheckResult smokeCheckResult) {
        var pingData = mapPingCheckResult(smokeCheckResult.pingCheckResult());
        //other mappings for DTO
        return new SmokeResponse(pingData);
    }

    private SmokeResponse.PingData mapPingCheckResult(
            SmokeCheckResult.PingCheckResult pingCheckResult
    ) {
        return new SmokeResponse.PingData(
                "ping",
                pingCheckResult.ok(),
                pingCheckResult.msg()
        );
    }
}
