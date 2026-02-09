package com.jay.template.bootstrap.observability.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "platform.observability")
@Validated
public record ObservabilityProperties(
        @NotNull Mdc mdc
) {
    public record Mdc(
            @NotBlank String userId,
            @NotBlank String requestId,
            @NotBlank String kind,
            @NotBlank String name,
            @NotBlank String method,
            @NotBlank String status,
            @NotBlank String durationMs,
            @NotNull @Valid KindValues kindValues
    ) {
        public record KindValues(
                @NotBlank String http
        ) {}
    }
}
