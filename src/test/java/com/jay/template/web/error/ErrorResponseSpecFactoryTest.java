package com.jay.template.web.error;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.core.error.api.ErrorType;
import com.jay.template.helper.MockTracerUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static com.jay.template.core.error.api.ErrorType.INTERNAL_SERVER_ERROR;

class ErrorResponseSpecFactoryTest {

    @Test
    void buildResponseSpecSetsStatusAndBody() {
        String traceId = "trace-001";
        Tracer tracer = MockTracerUtils.mockTracer(traceId);

        ErrorType type = INTERNAL_SERVER_ERROR;
        ErrorResponseSpecFactory factory = new ErrorResponseSpecFactory(tracer);
        ErrorResponseSpec spec = factory.buildResponseSpec(type);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, spec.status());

        ErrorResponse body = spec.body();
        assertEquals(type.name(), body.code());
        assertEquals(type.defaultMessage(), body.message());
        assertEquals(traceId, body.correlationId());
    }

    @Test
    void buildResponseSpecSetsNullCorrelationIdWhenNoCurrentSpan() {
        Tracer tracer = mock(Tracer.class);
        when(tracer.currentSpan()).thenReturn(null);

        ErrorType type = INTERNAL_SERVER_ERROR;
        ErrorResponseSpecFactory factory = new ErrorResponseSpecFactory(tracer);
        ErrorResponseSpec spec = factory.buildResponseSpec(type);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, spec.status());

        ErrorResponse body = spec.body();
        assertEquals(type.name(), body.code());
        assertEquals(type.defaultMessage(), body.message());
        assertNull(body.correlationId());
    }

}