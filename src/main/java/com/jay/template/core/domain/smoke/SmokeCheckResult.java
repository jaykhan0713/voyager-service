package com.jay.template.core.domain.smoke;

// aggregated model based on dependency models.
public record SmokeCheckResult(
        PingCheckResult pingCheckResult) {
    public record PingCheckResult(
            boolean ok,
            String msg
    ) {}

    //other check results go here
}
