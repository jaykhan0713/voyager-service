package com.jay.template.web.mvc.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.core.error.api.ApiException;
import com.jay.template.core.error.api.ErrorType;
import com.jay.template.web.error.ErrorResponseSpec;
import com.jay.template.web.error.ErrorResponseSpecFactory;

import static com.jay.template.core.error.api.ErrorType.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String API_EX_MESSAGE_FORMAT = "error.code={} root.cause={} ex.msg=\"{}\"";
    private static final String GENERIC_EX_MESSAGE_FORMAT = "error.code=";

    private final ErrorResponseSpecFactory errorResponseSpecFactory;

    public GlobalExceptionHandler(ErrorResponseSpecFactory errorResponseSpecFactory) {
        this.errorResponseSpecFactory = errorResponseSpecFactory;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ErrorType type = ex.type();

        // NOTE: expected error, no stack trace noise
        // optionally surface errorCode via MDC for identity-complete logging for MDCFilter

        // FUTURE-NOTE: enrich dependency failure logging (clientName, reason) if needed.
        // Current approach logs root cause only to avoid overengineering.
        String rootCauseName = rootCause(ex);

        // ApiException always carries a non-null message, defaulting to ErrorType.defaultMessage()
        LOGGER.error(
                API_EX_MESSAGE_FORMAT,
                type.name(),
                rootCauseName,
                ex.getMessage()
        );

        return buildResponseEntity(type);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        ErrorType type = INTERNAL_SERVER_ERROR;

        String msg = GENERIC_EX_MESSAGE_FORMAT + type.name();
        LOGGER.error(msg, ex);

        return buildResponseEntity(type);
    }

    private ResponseEntity<ErrorResponse> buildResponseEntity(ErrorType type) {
        ErrorResponseSpec spec = errorResponseSpecFactory.buildResponseSpec(type);

        return ResponseEntity
                .status(spec.status())
                .body(spec.body());
    }

    private String rootCause(Throwable throwable) {
        Throwable cause = throwable;

        while (cause.getCause() != null && cause.getCause() != cause) { //safeguard against cause loops.
            cause = cause.getCause();
        }

        return cause.getClass().getSimpleName();
    }
}
