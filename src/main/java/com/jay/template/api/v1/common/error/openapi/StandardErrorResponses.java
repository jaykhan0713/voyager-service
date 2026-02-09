package com.jay.template.api.v1.common.error.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.jay.template.api.v1.common.error.ErrorResponse;

/**
 * Declares the standard error responses shared by all v1 API endpoints.
 *
 * <p>This annotation centralizes common HTTP error documentation so individual
 * API methods only need to document success responses and endpoint-specific
 * errors (for example {@code 404}).</p>
 *
 * <p>Semantics:
 * <ul>
 *   <li>{@code 400} – client sent an invalid request</li>
 *   <li>{@code 429} – inbound bulkhead permit limit exceeded (service is saturated)</li>
 *   <li>{@code 500} – unexpected internal error</li>
 *   <li>{@code 503} – downstream dependency unavailable (circuit breaker,
 *       bulkhead, or I/O failure)</li>
 * </ul>
 * </p>
 *
 * <p>This annotation is for OpenAPI documentation only and has no effect on
 * runtime error handling.</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "429",
                description = "Too Many Requests",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "503",
                description = "Service Unavailable",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)
                )
        )
})
public @interface StandardErrorResponses {}