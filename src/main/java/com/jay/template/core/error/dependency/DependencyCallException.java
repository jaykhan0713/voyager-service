package com.jay.template.core.error.dependency;

import java.util.Objects;

/**
 * Typed exception representing a failure of an outbound dependency.
 *
 * <p>
 * {@code DependencyCallException} is intended to be thrown by infrastructure
 * adapters when an outbound call to an external system fails. It represents
 * the boundary at which transport-, protocol-, and resiliency-level failures
 * are normalized into a stable, application-facing error contract.
 * </p>
 *
 * <p>
 * A dependency is treated as a black box by the application. This exception
 * intentionally does not expose transport details, HTTP status codes, or
 * client-specific exception types. Instead, failures are categorized using
 * a small, explicit set of {@link Reason} values that describe the nature of
 * the dependency failure.
 * </p>
 *
 * <p>
 * {@code DependencyCallException} is not a business error. It represents a
 * technical failure that must be interpreted by the application orchestration
 * layer and acts as a bridge. The application orchestration layer is responsible for translating this exception
 * into a business-meaningful error (for example, an {@code ApiException})
 * appropriate for the inbound boundary.
 * </p>
 *
 * <p>
 * Infrastructure code should throw this exception at the point where a
 * dependency failure is detected. Lower-level libraries and transport-specific
 * exceptions should not be allowed to propagate beyond the infrastructure
 * layer.
 * </p>
 *
 * <p>
 * This type defines a stable error bridge between outbound adapters and the
 * application orchestration layer, allowing the rest of the system to reason
 * about dependency failures without knowledge of how those dependencies are
 * implemented.
 * </p>
 */
public class DependencyCallException extends RuntimeException {

    private final String clientName;
    private final Reason reason;

    public DependencyCallException(
            String clientName,
            Reason reason
    ) {
        this(clientName, reason, null);
    }

    public DependencyCallException(
            String clientName,
            Reason reason,
            Throwable cause
    ) {
        super(cause);
        this.clientName = Objects.requireNonNull(clientName);
        this.reason = Objects.requireNonNull(reason);
    }

    public String clientName() {
        return clientName;
    }

    public Reason reason() {
        return reason;
    }
}
