package com.jay.template.infra.outbound.http.client.resiliency;

import java.io.IOException;
import java.time.Duration;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestFactory;

import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;
import com.jay.template.infra.outbound.http.client.resiliency.circuitbreaker.CircuitBreakerRequestDecoratorFactory;
import com.jay.template.infra.outbound.http.client.resiliency.bulkhead.BulkheadRequestDecoratorFactory;

//orchestration of functional resiliency responsibilities
public class ResiliencyChainAssembler {

    //note that micrometer r4j metrics will use same instance name but give it distinct (bulkhead/cb) metrics
    private static final String INSTANCE_SUFFIX = "OutboundClient";

    private final BulkheadRegistry bulkheadRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResiliencyChainAssembler(
            BulkheadRegistry bulkheadRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        this.bulkheadRegistry = bulkheadRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public ClientHttpRequestFactory assemble(
            ClientHttpRequestFactory requestFactory,
            ResiliencyPolicy resiliencyPolicy,
            String clientName
    ) {
        String instanceName = createInstanceName(clientName);
        ClientHttpRequestFactory decorated = requestFactory;

        /*
         * Order matters:
         * Outermost should acquire permit with bulkhead first (fail-fast backpressure)
         * Then circuit breaker executes (bulkhead may still reject regardless of cb state)
         */
        decorated = applyCircuitBreaker(decorated, resiliencyPolicy.circuitBreakerPolicy(), instanceName);
        decorated = applyBulkhead(decorated, resiliencyPolicy.bulkheadPolicy(), instanceName);

        return decorated;
    }

    //bulkhead
    ClientHttpRequestFactory applyBulkhead(
            ClientHttpRequestFactory delegate,
            ResiliencyPolicy.BulkheadPolicy clientBulkheadPolicy,
            String instanceName
    ) {
        if (clientBulkheadPolicy.enabled()) {
            int maxConcurrentCalls =
                    clientBulkheadPolicy.maxConcurrentCalls();
            Duration maxWaitDuration =
                    clientBulkheadPolicy.maxWaitDuration();

            BulkheadConfig.Builder bulkheadConfigBuilder = new BulkheadConfig.Builder();
            bulkheadConfigBuilder.maxConcurrentCalls(maxConcurrentCalls);
            bulkheadConfigBuilder.maxWaitDuration(maxWaitDuration);

            //registry should create new instance
            Bulkhead bulkhead = bulkheadRegistry.bulkhead(instanceName, bulkheadConfigBuilder.build());

            return new BulkheadRequestDecoratorFactory(delegate, bulkhead);
        }

        return delegate;
    }

    // circuit breaker
    ClientHttpRequestFactory applyCircuitBreaker(
            ClientHttpRequestFactory delegate,
            ResiliencyPolicy.CircuitBreakerPolicy clientCircuitBreakerPolicy,
            String instanceName
    ) {
        if (clientCircuitBreakerPolicy.enabled()) {
            CircuitBreakerConfig.Builder cbConfigBuilder = CircuitBreakerConfig.custom();
            cbConfigBuilder.failureRateThreshold(clientCircuitBreakerPolicy.failureRateThreshold());
            cbConfigBuilder.slowCallDurationThreshold(clientCircuitBreakerPolicy.slowCallDurationThreshold());
            cbConfigBuilder.slowCallRateThreshold(clientCircuitBreakerPolicy.slowCallRateThreshold());

            cbConfigBuilder.slidingWindowType(
                    mapSlidingWindowType(clientCircuitBreakerPolicy.slidingWindowType())
            );
            cbConfigBuilder.slidingWindowSize(clientCircuitBreakerPolicy.slidingWindowSize());
            cbConfigBuilder.minimumNumberOfCalls(clientCircuitBreakerPolicy.minimumNumberOfCalls());

            cbConfigBuilder.permittedNumberOfCallsInHalfOpenState(
                    clientCircuitBreakerPolicy.permittedNumberOfCallsInHalfOpenState()
            );
            cbConfigBuilder.waitDurationInOpenState(clientCircuitBreakerPolicy.waitDurationInOpenState());

            /*
             * FUTURE-NOTE: Baseline behavior is to treat any 5xx HTTP response as a circuit breaker failure.
             *  When concrete use cases show up, introduce per-client settings to narrow recorded 5xx codes and
             *  optionally include select 4xx like 429 when appropriate.
             */
            cbConfigBuilder.recordResult(result -> {
                if (result instanceof ClientHttpResponse response) {
                    try {
                        if (response.getStatusCode().is5xxServerError()) {
                            return true;
                        }
                    } catch (IOException _) { /* getStatusCode() throwing is not job of this class to handle */ }
                }

                return false; //instance shouldn't be used outside of decorator, but harmless failsafe.
            });

            CircuitBreaker circuitBreaker =
                    circuitBreakerRegistry.circuitBreaker(instanceName, cbConfigBuilder.build());

            return new CircuitBreakerRequestDecoratorFactory(delegate, circuitBreaker);
        }

        return delegate;
    }

    private CircuitBreakerConfig.SlidingWindowType mapSlidingWindowType(
            ResiliencyPolicy.CircuitBreakerPolicy.SlidingWindowType type
    ) {
        return switch (type) {
            case COUNT_BASED -> CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
            case TIME_BASED -> CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
        };
    }

    private String createInstanceName(String clientName) {
        return clientName + INSTANCE_SUFFIX;
    }
}
