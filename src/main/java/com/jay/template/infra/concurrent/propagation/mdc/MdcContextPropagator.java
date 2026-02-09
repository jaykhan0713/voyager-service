package com.jay.template.infra.concurrent.propagation.mdc;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

import com.jay.template.infra.concurrent.propagation.ContextPropagator;

public final class MdcContextPropagator implements ContextPropagator {

    @Override
    public Runnable propagate(Runnable task) {
        Map<String, String> captured = MDC.getCopyOfContextMap(); // Calling thread's MDC
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                apply(captured);
                task.run();
            } finally {
                apply(previous);
            }
        };
    }

    @Override
    public <T> Callable<T> propagate(Callable<T> task) {
        Map<String, String> captured = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();

            try {
                apply(captured);
                return task.call();
            } finally {
                apply(previous);
            }
        };
    }

    private void apply(Map<String, String> contextMap) {
        if (contextMap == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(contextMap);
        }
    }
}
