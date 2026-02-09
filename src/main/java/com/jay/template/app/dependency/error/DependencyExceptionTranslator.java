package com.jay.template.app.dependency.error;

import java.util.function.Supplier;

import com.jay.template.core.error.api.ApiException;
import com.jay.template.core.error.api.ErrorType;
import com.jay.template.core.error.dependency.DependencyCallException;

public final class DependencyExceptionTranslator {

    private DependencyExceptionTranslator() {}

    public static <T> T execute(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (DependencyCallException ex) {
            //FUTURE-NOTE: Map based on reason as reason enum grows.
            throw new ApiException(ErrorType.DEPENDENCY_UNAVAILABLE, ex);
        }
    }
}