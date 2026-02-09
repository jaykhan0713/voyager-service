package com.jay.template.infra.outbound.http.client.rest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import com.jay.template.core.outbound.http.client.settings.HttpClientSettings;
import com.jay.template.core.outbound.resiliency.policy.ResiliencyPolicy;
import com.jay.template.infra.outbound.http.client.interceptor.IdentityHeaderInterceptor;
import com.jay.template.infra.outbound.http.client.registry.HttpClientSettingsRegistry;
import com.jay.template.infra.outbound.http.client.resiliency.ResiliencyChainAssembler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RestClientFactoryTest {

    @Test
    void buildsClientWithDefaultInterceptors() {

        RestClient.Builder rootBuilder = mock(RestClient.Builder.class);
        RestClient.Builder builder = mock(RestClient.Builder.class);

        HttpClientSettingsRegistry clientSettingsRegistry = mock(HttpClientSettingsRegistry.class);
        HttpClientSettings httpClientSettings = new HttpClientSettings(
                "testClient",
                "test-base-url.com",
                Duration.ofSeconds(10),
                Duration.ofSeconds(10),
                mock(ResiliencyPolicy.class)
        );

        ClientHttpRequestInterceptor identityHeaderInterceptor = mock(IdentityHeaderInterceptor.class);
        ClientHttpRequestInterceptor otherInterceptor = mock(ClientHttpRequestInterceptor.class);
        List<ClientHttpRequestInterceptor> interceptors = List.of(identityHeaderInterceptor, otherInterceptor);

        ResiliencyChainAssembler resiliencyChainAssembler = mock(ResiliencyChainAssembler.class);
        ClientHttpRequestFactory decoratorFactory = mock(ClientHttpRequestFactory.class);

        RestClient expectedRestClient = mock(RestClient.class);

        when(clientSettingsRegistry.httpClientSettings(anyString()))
                .thenReturn(httpClientSettings);
        when(rootBuilder.clone()).thenReturn(builder);
        when(resiliencyChainAssembler.assemble(any(), any(), anyString())).thenReturn(decoratorFactory);
        when(builder.requestFactory(any())).thenReturn(builder);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.requestInterceptors(any())).thenReturn(builder);
        when(builder.build()).thenReturn(expectedRestClient);

        RestClientFactory factory = new RestClientFactory(
                rootBuilder, clientSettingsRegistry, interceptors, resiliencyChainAssembler
        );

        RestClient builtRestClient = factory.buildClient("clientNameKey");

        assertSame(expectedRestClient, builtRestClient);

        verify(rootBuilder).clone();
        verify(builder).requestFactory(same(decoratorFactory));
        verify(resiliencyChainAssembler).assemble(
                any(),
                eq(httpClientSettings.resiliencyPolicy()),
                eq(httpClientSettings.clientName())
        );
        verify(builder).baseUrl(httpClientSettings.baseUrl());
        verify(builder).build();

        //verify interceptors

        @SuppressWarnings("unchecked")
        //ArgumentCaptor<T> where T means what was passed into the method
        ArgumentCaptor<Consumer<List<ClientHttpRequestInterceptor>>> captor =
                ArgumentCaptor.forClass(Consumer.class);

        verify(builder).requestInterceptors(captor.capture()); //verifies method was called once, then capture
        ClientHttpRequestInterceptor existing = mock(ClientHttpRequestInterceptor.class);
        List<ClientHttpRequestInterceptor> applied = new ArrayList<>();
        applied.add(existing);
        captor.getValue().accept(applied);

        assertSame(existing, applied.get(0));
        assertSame(identityHeaderInterceptor, applied.get(1));
        assertSame(otherInterceptor, applied.get(2));
    }
}
