package com.jay.template.web.mvc.controller.smoke.api.model;

// Dummy response contract for inbound request, would use openapi or other contract api in real scenario
public record SmokeResponse(PingData pingData) {
    public record PingData(
            String businessPath,
            boolean success,
            String message
    ) {}
}
