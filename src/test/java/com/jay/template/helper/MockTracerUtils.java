package com.jay.template.helper;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.mockito.Mockito;

public final class MockTracerUtils {

    private MockTracerUtils() {}

    public static Tracer mockTracer(String traceId) {
        Tracer tracer = Mockito.mock(Tracer.class);
        Span span = Mockito.mock(Span.class);
        TraceContext context = Mockito.mock(TraceContext.class);

        Mockito.when(tracer.currentSpan()).thenReturn(span);
        Mockito.when(span.context()).thenReturn(context);
        Mockito.when(context.traceId()).thenReturn(traceId);

        return tracer;
    }
}
