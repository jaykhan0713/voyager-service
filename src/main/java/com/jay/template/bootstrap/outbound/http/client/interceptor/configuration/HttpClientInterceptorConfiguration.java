package com.jay.voyager.bootstrap.outbound.http.client.interceptor.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.jay.voyager.core.port.transport.http.IdentityHeadersProvider;
import com.jay.voyager.infra.outbound.http.client.interceptor.IdentityHeaderInterceptor;

@Configuration
public class HttpClientInterceptorConfiguration {

    @Bean
    public ClientHttpRequestInterceptor identityHeaderInterceptor(IdentityHeadersProvider headersProvider) {
        return new IdentityHeaderInterceptor(headersProvider.identityHeaders());
    }

    @Bean("defaultHttpClientRequestInterceptors")
    public List<ClientHttpRequestInterceptor> defaultHttpClientRequestInterceptors (
        ClientHttpRequestInterceptor identityHeaderInterceptor
    ) {
        return List.of(identityHeaderInterceptor);
    }
}
