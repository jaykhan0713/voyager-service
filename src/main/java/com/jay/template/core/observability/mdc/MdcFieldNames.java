package com.jay.template.core.observability.mdc;

public record MdcFieldNames(
        String userId,
        String requestId,
        String kind,
        String name,
        String method,
        String status,
        String durationMs,
        KindValues kindValues
) {
    public record KindValues(String http) {}
}
