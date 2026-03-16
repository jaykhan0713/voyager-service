package com.jay.voyager.bootstrap.observability.metrics.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.java21.instrument.binder.jdk.VirtualThreadMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VirtualThreadMetricsConfig {
    @Bean
    VirtualThreadMetrics virtualThreadMetrics(MeterRegistry registry) {
        var metrics = new VirtualThreadMetrics();
        metrics.bindTo(registry);
        return metrics;
    }
}