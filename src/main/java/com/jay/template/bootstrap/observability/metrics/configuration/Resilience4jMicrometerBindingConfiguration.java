package com.jay.template.bootstrap.observability.metrics.configuration;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedBulkheadMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bootstrap configuration that binds Resilience4j registries to Micrometer.
 *
 * <p>
 * Resilience4j does not automatically publish metrics to Micrometer unless an
 * explicit metrics binder is registered.
 * </p>
 *
 * <p>
 * This configuration explicitly bridges:
 * <ul>
 *   <li>{@link io.github.resilience4j.bulkhead.BulkheadRegistry}</li>
 *   <li>{@link io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry}</li>
 * </ul>
 * to the application {@link io.micrometer.core.instrument.MeterRegistry}
 * using Resilience4j's tagged Micrometer binders.
 * </p>
 *
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Expose Resilience4j metrics under /actuator/prometheus</li>
 *   <li>Ensure a single, deterministic metrics binding</li>
 *   <li>Avoid duplicate meter registration warnings</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class intentionally lives in the bootstrap layer so that:
 * <ul>
 *   <li>Infra remains framework-agnostic</li>
 *   <li>Observability wiring is centralized</li>
 *   <li>Spring lifecycle concerns do not leak into core or infra</li>
 * </ul>
 * </p>
 */
@Configuration
public class Resilience4jMicrometerBindingConfiguration {

    @Bean
    public TaggedBulkheadMetrics taggedBulkheadMetrics(
            BulkheadRegistry bulkheadRegistry,
            MeterRegistry meterRegistry
    ) {
        TaggedBulkheadMetrics metrics = TaggedBulkheadMetrics.ofBulkheadRegistry(bulkheadRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }

    @Bean
    public TaggedCircuitBreakerMetrics taggedCircuitBreakerMetrics(
            CircuitBreakerRegistry circuitBreakerRegistry,
            MeterRegistry meterRegistry
    ) {
        TaggedCircuitBreakerMetrics metrics =
                TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(circuitBreakerRegistry);
        metrics.bindTo(meterRegistry);
        return metrics;
    }
}
