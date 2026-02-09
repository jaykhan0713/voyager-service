package com.jay.template.bootstrap.dependency.ping.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.jay.template.core.port.dependency.ping.PingDependency;
import com.jay.template.infra.outbound.http.client.rest.RestClientFactory;
import com.jay.template.infra.outbound.http.client.rest.adapter.ping.mapping.DownstreamPingResponseMapper;
import com.jay.template.infra.outbound.http.client.rest.adapter.ping.PingRestClientAdapter;

@Configuration
@Profile("smoke")
public class PingDependencyConfiguration {

    private static final String CLIENT_NAME = "ping";
    private static final String PING_URI = "/api/v1/ping";

    @Bean
    public PingDependency pingDependency(RestClientFactory restClientFactory) {
        return new PingRestClientAdapter(
                restClientFactory.buildClient(CLIENT_NAME),
                CLIENT_NAME,
                PING_URI,
                new DownstreamPingResponseMapper()
        );
    }
}
