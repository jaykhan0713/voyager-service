package com.jay.template.core.error.api;

import java.util.Objects;

/**
 * Typed exception representing an application-level failure.
 *
 * <p>
 * {@code ApiException} is intended to be thrown by the business orchestration
 * layer (app) to signal a failure that should be surfaced to an inbound boundary,
 * such as a global exception handler.
 * </p>
 *
 * <p>
 * The exception carries a mandatory {@link ErrorType} that describes the semantic
 * category of the failure. Inbound layers are responsible for translating this
 * type into protocol-specific responses, such as HTTP status codes and
 * standardized error payloads.
 * </p>
 *
 * <p>
 * {@code ApiException} represents the point at which technical and dependency
 * failures are normalized into business-meaningful error categories. Lower
 * layers should not throw this exception directly, but instead allow failures
 * to be translated at the application orchestration boundary.
 * </p>
 *
 * <p>
 * This type defines a stable error contract shared between the business
 * orchestration layer and inbound adapters.
 * </p>
 */
public final class ApiException extends RuntimeException {

    private final ErrorType type;

    public ApiException(ErrorType type) {
        super(Objects.requireNonNull(type).defaultMessage());
        this.type = type;
    }

    public ApiException(ErrorType type, String customMessage) {
        super(customMessage);
        this.type = Objects.requireNonNull(type);
    }

    public ApiException(ErrorType type, Throwable cause) {
        this(Objects.requireNonNull(type), type.defaultMessage(), cause);
    }

    public ApiException(ErrorType type, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.type = Objects.requireNonNull(type);
    }

    public ErrorType type() {
        return type;
    }
}
