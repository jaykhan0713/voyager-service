package com.jay.template.bootstrap.outbound.http.properties;

import java.time.Duration;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.jay.template.bootstrap.outbound.resiliency.properties.ResiliencyProperties;

@ConfigurationProperties(prefix = "platform.outbound.http")
@Validated
public record OutboundHttpProperties(
        @NotNull @Valid ClientDefaults clientDefaults,
        @Valid Map<String, Client> clients // add @NotEmpty if service always expects clients.
) {

    /*
     * Per client settings related
     */
    public record ClientDefaults(
            @NotNull Duration connectTimeout,
            @NotNull Duration readTimeout,
            // Only defaults cascade @Validation; ClientConfig resiliency may override or use defaults
            @NotNull @Valid ResiliencyProperties resiliency
    ) {}

    public record Client(
            @NotNull String baseUrl,
            Duration connectTimeout,
            Duration readTimeout,
            // Intentionally no @Valid, allow partial overrides, defaults applied in resolver
            ResiliencyProperties resiliency
    ) {
        public Duration connectTimeoutOrDefault(ClientDefaults defaults) {
            return connectTimeout == null ? defaults.connectTimeout() : connectTimeout;
        }

        public Duration readTimeoutOrDefault(ClientDefaults defaults) {
            return readTimeout == null ? defaults.readTimeout() : readTimeout;
        }

        public ResiliencyProperties resiliencyOrDefault(ClientDefaults defaults) {
            return resiliency == null ? defaults.resiliency() : resiliency;
        }
    }
}
