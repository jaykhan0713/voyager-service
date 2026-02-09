package com.jay.template.core.outbound.resiliency.policy;

import java.time.Duration;

public record ResiliencyPolicy(
        BulkheadPolicy bulkheadPolicy,
        CircuitBreakerPolicy circuitBreakerPolicy
) {
    public record BulkheadPolicy(
            boolean enabled,
            int maxConcurrentCalls,
            Duration maxWaitDuration
    ) {}

    public record CircuitBreakerPolicy(
            boolean enabled,

            int failureRateThreshold,

            Duration slowCallDurationThreshold,
            int slowCallRateThreshold,

            SlidingWindowType slidingWindowType,
            int slidingWindowSize,
            int minimumNumberOfCalls,

            int permittedNumberOfCallsInHalfOpenState,
            Duration waitDurationInOpenState
    ) {
        public enum SlidingWindowType {
            COUNT_BASED,
            TIME_BASED
        }
    }
}
