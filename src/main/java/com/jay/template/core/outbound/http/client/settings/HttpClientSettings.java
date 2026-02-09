package com.jay.template.core.outbound.http.client.settings;

import java.time.Duration;

import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;

public record HttpClientSettings(
        String clientName,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        ResiliencyPolicy resiliencyPolicy
) {}
