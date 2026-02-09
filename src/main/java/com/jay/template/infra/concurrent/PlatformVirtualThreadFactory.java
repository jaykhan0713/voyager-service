package com.jay.template.infra.concurrent;

import java.util.List;
import java.util.concurrent.ThreadFactory;

import com.jay.template.infra.concurrent.propagation.ContextPropagator;

public final class PlatformVirtualThreadFactory implements ThreadFactory {

    private final ThreadFactory delegate;
    private final List<ContextPropagator> propagators;

    public PlatformVirtualThreadFactory(List<ContextPropagator> propagators) {
        this.delegate = Thread.ofVirtual().factory();
        this.propagators = List.copyOf(propagators);
    }

    @Override
    public Thread newThread(Runnable task) {
        Runnable propagatedTask = task;
        for (ContextPropagator propagator : propagators) {
            propagatedTask = propagator.propagate(propagatedTask);
        }
        return delegate.newThread(propagatedTask);
    }
}
