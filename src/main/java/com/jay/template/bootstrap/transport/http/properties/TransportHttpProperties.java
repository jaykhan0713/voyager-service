package com.jay.template.bootstrap.transport.http.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.transport")
@Validated
public record TransportHttpProperties(
        @NotNull @Valid Http http
) {
    public record Http(
            @NotNull @Valid Headers headers
    ) {
        public record Headers(
                @NotBlank String userId,
                @NotBlank String requestId
        ) {}
    }
}
