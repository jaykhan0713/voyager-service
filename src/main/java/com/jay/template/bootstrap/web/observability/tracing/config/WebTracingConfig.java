package com.jay.template.bootstrap.web.observability.tracing.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Configuration
public class WebTracingConfig {

    private static final String HTTP_SERVER_REQUESTS = "http.server.requests";

    // this will make sure that only http requests at /api/* have traces generated for it
    @Bean
    ObservationRegistryCustomizer<ObservationRegistry> traceOnlyApiRequests() {
        return registry ->
                registry.observationConfig().observationPredicate((name, context) -> {

                    if (HTTP_SERVER_REQUESTS.equals(name)
                            && (context instanceof  ServerRequestObservationContext serverContext)
                    ) {
                        String uri = serverContext.getCarrier().getRequestURI();

                        // Intent boundary: only trace API traffic
                        return uri.startsWith("/api/");
                    }

                    return true;
                });
    }
}
