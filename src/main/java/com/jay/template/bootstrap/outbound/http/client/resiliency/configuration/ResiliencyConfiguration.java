package com.jay.template.bootstrap.outbound.http.client.resiliency.configuration;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainAssembler;

@Configuration
public class ResiliencyConfiguration {

    @Bean
    ResiliencyChainAssembler resiliencyDecorator(
            BulkheadRegistry bulkheadRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry
    ) {
        return new ResiliencyChainAssembler(
                bulkheadRegistry,
                circuitBreakerRegistry
        );
    }
}
