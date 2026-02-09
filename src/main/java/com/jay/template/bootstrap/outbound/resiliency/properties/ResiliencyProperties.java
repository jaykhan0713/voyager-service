package com.jay.template.bootstrap.outbound.resiliency.properties;

import java.time.Duration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Protocol-agnostic resiliency configuration used by outbound adapters.
 *
 * <p>
 * This type intentionally serves two roles depending on where it is used:
 * </p>
 *
 * <ul>
 *   <li>
 *     <b>Defaults (strict validation)</b>:
 *     When referenced from a {@code @Validated} defaults object and annotated with {@code @Valid},
 *     all {@code @NotNull} constraints in this type are enforced. This guarantees that platform-wide
 *     resiliency defaults are fully specified and safe to use without defensive null checks.
 *   </li>
 *
 *   <li>
 *     <b>Overrides (partial allowed)</b>:
 *     When referenced from a per-client override without {@code @Valid}, validation does not cascade.
 *     In this mode, this type is allowed to be partial. Missing fields are resolved at runtime by
 *     explicitly falling back to defaults via the {@code *OrDefault(...)} helpers.
 *   </li>
 * </ul>
 *
 * <p>
 * This design intentionally avoids validation groups or separate "defaults vs override" types.
 * Instead, validation behavior is controlled by where {@code @Valid} is applied.
 * </p>
 *
 * <p>
 * IMPORTANT:
 * <ul>
 *   <li>Callers must never read fields directly from override instances.</li>
 *   <li>All access must go through {@code *OrDefault(...)} methods during resolution.</li>
 *   <li>This type is not a resolved runtime model; it is a bootstrap configuration carrier.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Failure classification (for example HTTP status codes, DB exception categories, etc.)
 * is intentionally not modeled here. This type only defines protocol-agnostic resiliency knobs.
 * Protocol-specific failure semantics are applied later when building concrete adapters.
 * </p>
 */
public record ResiliencyProperties(
        @NotNull @Valid Bulkhead bulkhead,
        @NotNull @Valid CircuitBreaker circuitBreaker
) {

    public record Bulkhead(
            @NotNull Boolean enabled,
            @NotNull Integer maxConcurrentCalls,
            @NotNull Duration maxWaitDuration
    ) {
        public Boolean enabledOrDefault(Bulkhead defaults) {
            return enabled == null ? defaults.enabled() : enabled;
        }

        public Integer maxConcurrentCallsOrDefault(Bulkhead defaults) {
            return maxConcurrentCalls == null ? defaults.maxConcurrentCalls() : maxConcurrentCalls;
        }

        public Duration maxWaitDurationOrDefault(Bulkhead defaults) {
            return maxWaitDuration == null ? defaults.maxWaitDuration() : maxWaitDuration;
        }
    }

    public record CircuitBreaker(
            @NotNull Boolean enabled,

            @NotNull Integer failureRateThreshold,

            @NotNull Duration slowCallDurationThreshold,
            @NotNull Integer slowCallRateThreshold,

            @NotNull SlidingWindowType slidingWindowType,
            @NotNull Integer slidingWindowSize,
            @NotNull Integer minimumNumberOfCalls,

            @NotNull Integer permittedNumberOfCallsInHalfOpenState,
            @NotNull Duration waitDurationInOpenState
    ) {

        public Boolean enabledOrDefault(CircuitBreaker defaults) {
            return enabled == null ? defaults.enabled() : enabled;
        }

        public Integer failureRateThresholdOrDefault(CircuitBreaker defaults) {
            return failureRateThreshold == null ? defaults.failureRateThreshold() : failureRateThreshold;
        }

        public Duration slowCallDurationThresholdOrDefault(CircuitBreaker defaults) {
            return slowCallDurationThreshold == null
                    ? defaults.slowCallDurationThreshold()
                    : slowCallDurationThreshold;
        }

        public Integer slowCallRateThresholdOrDefault(CircuitBreaker defaults) {
            return slowCallRateThreshold == null
                    ? defaults.slowCallRateThreshold()
                    : slowCallRateThreshold;
        }

        public SlidingWindowType slidingWindowTypeOrDefault(CircuitBreaker defaults) {
            return slidingWindowType == null
                    ? defaults.slidingWindowType()
                    : slidingWindowType;
        }

        public Integer slidingWindowSizeOrDefault(CircuitBreaker defaults) {
            return slidingWindowSize == null
                    ? defaults.slidingWindowSize()
                    : slidingWindowSize;
        }

        public Integer minimumNumberOfCallsOrDefault(CircuitBreaker defaults) {
            return minimumNumberOfCalls == null
                    ? defaults.minimumNumberOfCalls()
                    : minimumNumberOfCalls;
        }

        public Integer permittedNumberOfCallsInHalfOpenStateOrDefault(CircuitBreaker defaults) {
            return permittedNumberOfCallsInHalfOpenState == null
                    ? defaults.permittedNumberOfCallsInHalfOpenState()
                    : permittedNumberOfCallsInHalfOpenState;
        }

        public Duration waitDurationInOpenStateOrDefault(CircuitBreaker defaults) {
            return waitDurationInOpenState == null
                    ? defaults.waitDurationInOpenState()
                    : waitDurationInOpenState;
        }

        public enum SlidingWindowType {
            COUNT_BASED,
            TIME_BASED
        }
    }

    //resiliency type defaults:

    public Bulkhead bulkheadOrDefault(ResiliencyProperties defaults) {
        return bulkhead == null ? defaults.bulkhead() : bulkhead;
    }

    public CircuitBreaker circuitBreakerOrDefault(ResiliencyProperties defaults) {
        return circuitBreaker == null ? defaults.circuitBreaker() : circuitBreaker;
    }
}