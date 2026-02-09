package com.jay.template.bootstrap.outbound.http.client.rest.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import com.jay.template.infra.outbound.http.client.registry.HttpClientSettingsRegistry;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainAssembler;
import com.jay.template.infra.outbound.http.client.rest.RestClientFactory;

@Configuration
public class RestClientFactoryConfiguration {

    @Bean
    public RestClientFactory restClientFactory(
            RestClient.Builder restClientBuilder,
            HttpClientSettingsRegistry httpClientSettingsRegistry,
            @Qualifier("defaultHttpClientRequestInterceptors")
            List<ClientHttpRequestInterceptor> defaultRequestInterceptors,
            ResiliencyChainAssembler resiliencyChainAssembler
    ) {
        return new RestClientFactory(
                restClientBuilder,
                httpClientSettingsRegistry,
                defaultRequestInterceptors,
                resiliencyChainAssembler
        );
    }
}
