package com.jay.template.bootstrap.outbound.http.client.registry.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jay.template.core.port.outbound.http.client.HttpClientSettingsProvider;
import com.jay.template.infra.outbound.http.client.registry.HttpClientSettingsRegistry;

@Configuration
public class HttpClientSettingsRegistryConfiguration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HttpClientSettingsRegistryConfiguration.class);

    @Bean
    public HttpClientSettingsRegistry httpClientSettingsRegistry(
            HttpClientSettingsProvider provider
    ) {
        return new HttpClientSettingsRegistry(provider);
    }

    @Bean
    public SmartInitializingSingleton httpClientSettingsRegistryStartupValidator(
            HttpClientSettingsRegistry registry
    ) {
        return () -> {
            if (registry.clientNames().isEmpty()) {
                LOGGER.info("No outbound HTTP clients configured.");
            }

            LOGGER.info(
                    "HTTP client settings registry initialized with clients={}",
                    registry.clientNames()
            );
        };
    }
}
