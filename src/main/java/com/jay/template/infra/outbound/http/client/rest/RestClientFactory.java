package com.jay.template.infra.outbound.http.client.rest;

import java.net.http.HttpClient;
import java.util.List;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.infra.outbound.http.client.registry.HttpClientSettingsRegistry;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainAssembler;

public class RestClientFactory {

    private final RestClient.Builder restClientBuilder;
    private final HttpClientSettingsRegistry clientSettingsRegistry;
    private final ResiliencyChainAssembler resiliencyChainAssembler;
    private final List<ClientHttpRequestInterceptor> defaultRequestInterceptors;

    public RestClientFactory(
            RestClient.Builder restClientBuilder,
            HttpClientSettingsRegistry clientSettingsRegistry,
            List<ClientHttpRequestInterceptor> defaultRequestInterceptors,
            ResiliencyChainAssembler resiliencyChainAssembler
    ) {
        this.restClientBuilder = restClientBuilder;
        this.clientSettingsRegistry = clientSettingsRegistry;
        this.defaultRequestInterceptors = defaultRequestInterceptors;
        this.resiliencyChainAssembler = resiliencyChainAssembler;
    }

    public RestClient buildClient(String clientName) {
        return buildClient(clientName, defaultRequestInterceptors);
    }

    public RestClient buildClient(
            String clientName,
            List<ClientHttpRequestInterceptor> requestInterceptors
    ) {
        var settings = clientSettingsRegistry.httpClientSettings(clientName);

        /* Start from Spring Boot's auto-configured builder to preserve settings and micrometer/Otel instrumentation
         * Micrometer / OpenTelemetry ping span instrumentation
         * MUST use clone as injected RestClient.Builder is a mutable singleton
         */
        RestClient.Builder builder = restClientBuilder.clone();

        ClientHttpRequestFactory requestFactory = createJdkHttpClientFactory(settings);
        requestFactory = resiliencyChainAssembler.assemble(
                requestFactory,
                settings.resiliencyPolicy(),
                settings.clientName()
        );
        builder.requestFactory(requestFactory)
                .baseUrl(settings.baseUrl());

        builder.requestInterceptors(list -> list.addAll(requestInterceptors));

        return builder.build();
    }

    // signal intent: JDK Http Client usage default, but may want different Http clients in future.
    private ClientHttpRequestFactory createJdkHttpClientFactory(
            HttpClientSettings settings
    ) {
        var connectTimeout = settings.connectTimeout();
        var readTimeout = settings.readTimeout();

        /*
         * connectTimeout bounds the entire connection-establishment attempt (DNS + TCP + TLS if https).
         * This is I/O: with virtual threads the caller is typically parked while the OS/network stack
         * completes the connect.
         * JDK HttpClient does not expose a separate “wait for pooled connection” timeout like i.e Apache clients.
         */

        //transport creation, JDK HttpClient
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        // Bridge JDK HttpClient to Spring.
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return requestFactory;
    }
}
