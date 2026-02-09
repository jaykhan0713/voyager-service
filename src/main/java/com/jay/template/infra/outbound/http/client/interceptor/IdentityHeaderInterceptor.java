package com.jay.template.infra.outbound.http.client.interceptor;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.jay.template.core.context.identity.Identity;
import com.jay.template.core.context.identity.IdentityContextHolder;
import com.jay.template.core.transport.http.IdentityHeaders;

public class IdentityHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final IdentityHeaders identityHeaders;

    public IdentityHeaderInterceptor(IdentityHeaders identityHeaders) {
        this.identityHeaders = identityHeaders;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders httpHeaders = request.getHeaders();
        Identity identity = IdentityContextHolder.context().identity();

        httpHeaders.set(identityHeaders.userId(), identity.userId());
        httpHeaders.set(identityHeaders.requestId(), identity.requestId());

        return execution.execute(request, body);
    }
}
