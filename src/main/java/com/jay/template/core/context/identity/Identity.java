package com.jay.template.core.context.identity;

/**
 * Immutable identity.
 *
 * <p>
 * {@code Identity} represents identity-scoped identity metadata as known to the service.
 * It may originate from an API gateway, reverse proxy, or another upstream service.
 * </p>
 *
 * <p>
 * All fields are guaranteed to be non-null. Incoming {@code null} values are normalized
 * to empty strings during construction.
 * </p>
 *
 * <p>
 * An {@code Identity} is considered <em>empty</em> only when all of its fields are blank.
 * Partial identity is valid and represents available identity propagation.
 * </p>
 *
 * <p>
 * {@code Identity} is immutable for the lifetime of a identity and is intended for
 * correlation, tracing, auditing, and logging purposes.
 * </p>
 *
 * @param userId identifier of the calling user or service, if available
 * @param requestId correlation identifier for the identity, if available
 */
public record Identity(String userId, String requestId /*, future fields */) {

    public Identity {
        userId = normalizeIdentityValue(userId);
        requestId = normalizeIdentityValue(requestId);
    }

    /**
     * Indicates whether this identity carries no meaningful identity information.
     *
     * <p>
     * An identity is considered empty only if all fields are blank.
     * </p>
     *
     * @return {@code true} if all identity fields are blank, otherwise {@code false}
     */
    public boolean isEmpty() {
        return userId.isBlank()
                && requestId.isBlank();
                // && futureField.isBlank()
    }

    private static String normalizeIdentityValue(String value) {
        return value == null ? "" : value;
    }
}