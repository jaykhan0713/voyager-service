package com.jay.template.app.dependency.error;

import org.junit.jupiter.api.Test;

import com.jay.template.core.error.api.ApiException;
import com.jay.template.core.error.api.ErrorType;
import com.jay.template.core.error.dependency.DependencyCallException;
import com.jay.template.core.error.dependency.Reason;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DependencyExceptionTranslatorTest {

    @Test
    void supplierReturnsObject() {
        Object o = new Object();
        Object result = DependencyExceptionTranslator.execute(() -> o);
        assertSame(o, result);
    }

    @Test
    void supplierThrowsDependencyException() {
        ApiException ex = assertThrows(ApiException.class,
                () -> DependencyExceptionTranslator
                        .execute(() -> {
                            throw new DependencyCallException("someClient", Reason.IO_ERROR);
                        })
        );

        assertSame(ErrorType.DEPENDENCY_UNAVAILABLE, ex.type());
        assertInstanceOf(DependencyCallException.class, ex.getCause());
    }
}