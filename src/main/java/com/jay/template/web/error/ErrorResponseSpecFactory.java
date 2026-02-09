package com.jay.template.web.error;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.http.HttpStatus;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.core.error.api.ErrorType;

public class ErrorResponseSpecFactory {

    private static final ErrorTypeHttpStatusMapper STATUS_MAPPER = new ErrorTypeHttpStatusMapper();

    private final Tracer tracer;

    public ErrorResponseSpecFactory(Tracer tracer) {
        this.tracer = tracer;
    }

    public ErrorResponseSpec buildResponseSpec(ErrorType type) {

        Span span = tracer.currentSpan();
        String traceId = (span != null) ? span.context().traceId() : null;

        //Don't expose server errors to client, so body uses defaultMessage(). Log real error.

        ErrorResponse body = new ErrorResponse(type.name(), type.defaultMessage(), traceId);
        HttpStatus httpStatus = STATUS_MAPPER.mapErrorTypeToHttpStatus(type);

        return new ErrorResponseSpec(httpStatus, body);
    }
}
