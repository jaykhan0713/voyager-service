package com.jay.template.infra.outbound.http.client.rest.adapter.ping;

import org.springframework.web.client.RestClient;

import com.jay.template.core.domain.dependency.ping.PingResult;
import com.jay.template.core.port.dependency.ping.PingDependency;
import com.jay.template.infra.outbound.http.client.rest.error.RestClientExceptionTranslator;
import com.jay.template.infra.outbound.http.client.rest.adapter.ping.contract.DownstreamPingResponse;
import com.jay.template.infra.outbound.http.client.rest.adapter.ping.mapping.DownstreamPingResponseMapper;

public class PingRestClientAdapter implements PingDependency {

    private final RestClient restClient;
    private final String clientName;
    private final String uri;
    private final DownstreamPingResponseMapper dtoMapper;

    public PingRestClientAdapter(
            RestClient restClient,
            String clientName,
            String uri,
            DownstreamPingResponseMapper dtoMapper
    ) {
        this.restClient = restClient;
        this.clientName = clientName;
        this.uri = uri;
        this.dtoMapper = dtoMapper;
    }

    @Override
    public PingResult ping() {

        DownstreamPingResponse response =
                RestClientExceptionTranslator.execute(
                        () -> {
                            var spec = restClient
                                    .get()
                                    .uri(uri)
                                    .retrieve();

                            spec = RestClientExceptionTranslator.applyDefaultOnStatusHandlers(spec, clientName);

                            return spec.body(DownstreamPingResponse.class);
                        },
                        clientName
                );

        return dtoMapper.map(response);
    }
}
