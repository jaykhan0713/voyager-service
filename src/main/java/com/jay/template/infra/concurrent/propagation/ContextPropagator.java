package com.jay.template.infra.concurrent.propagation;

import java.util.concurrent.Callable;

public interface ContextPropagator {

    Runnable propagate(Runnable task);

    <T> Callable<T> propagate(Callable<T> task);
}
