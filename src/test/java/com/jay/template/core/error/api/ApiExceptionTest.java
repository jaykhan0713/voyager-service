package com.jay.template.core.error.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApiExceptionTest {

    private static final String CUSTOM_MESSAGE = "custom message";

    @Test
    void ctorRequiresNonNullErrorType() {
        assertThrows(NullPointerException.class, () -> new ApiException(null));
        assertThrows(NullPointerException.class, () -> new ApiException(null, CUSTOM_MESSAGE));
        RuntimeException cause = new RuntimeException();
        assertThrows(NullPointerException.class, () -> new ApiException(null, cause));
        assertThrows(NullPointerException.class, () -> new ApiException(null, CUSTOM_MESSAGE, cause));
    }

    @Test
    void ctorWithErrorType() {
        ApiException exception = new ApiException(ErrorType.BAD_REQUEST);
        assertEquals(ErrorType.BAD_REQUEST, exception.type());
        assertEquals(ErrorType.BAD_REQUEST.defaultMessage(), exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void ctorWithErrorTypeAndCustomMessage() {
        ApiException exception = new ApiException(ErrorType.BAD_REQUEST, CUSTOM_MESSAGE);
        assertEquals(ErrorType.BAD_REQUEST, exception.type());
        assertEquals(CUSTOM_MESSAGE, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void ctorWithErrorTypeAndThrowable() {
        RuntimeException cause = new RuntimeException();
        ApiException exception = new ApiException(ErrorType.BAD_REQUEST, cause);
        assertSame(ErrorType.BAD_REQUEST, exception.type());
        assertEquals(ErrorType.BAD_REQUEST.defaultMessage(), exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void ctorWithErrorTypeAndCustomMessageAndThrowable() {
        RuntimeException cause = new RuntimeException();
        ApiException exception = new ApiException(ErrorType.BAD_REQUEST, CUSTOM_MESSAGE, cause);
        assertEquals(ErrorType.BAD_REQUEST, exception.type());
        assertEquals(CUSTOM_MESSAGE, exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}