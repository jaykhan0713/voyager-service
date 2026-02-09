package com.jay.template.infra.outbound.http.client.rest.error;

import java.util.function.Supplier;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.jay.template.core.error.dependency.DependencyCallException;
import com.jay.template.core.error.dependency.Reason;

public final class RestClientExceptionTranslator {

    private RestClientExceptionTranslator() {}

    public static <T> T execute(
            Supplier<T> supplier,
            String clientName
    ) {
        try {
            return supplier.get();
        } catch (ResourceAccessException ex) {
            /*NOTE: Must handle wrapped IO exceptions similarly for other http client adapters if used in the future
             * ResourceAccessException is Spring's IOException contract for RestClient
             */
            //IO Exceptions like ConnectException, SocketException, DNS/handshake/connection refused etc.
            throw new DependencyCallException(clientName, Reason.IO_ERROR, ex);
        } catch (BulkheadFullException ex) {
            throw new DependencyCallException(clientName, Reason.CAPACITY_REJECTED, ex);
        } catch (CallNotPermittedException ex) {
            throw new DependencyCallException(clientName, Reason.SHORT_CIRCUITED, ex);
        } catch (RuntimeException ex) {
            throw new DependencyCallException(clientName,  Reason.UNKNOWN, ex);
        }
    }

    /*
     * FUTURE-NOTE: Per-client error handling is contract-specific.
     *  Default behavior here is to throw for 4xx/5xx.
     *  If a specific downstream has a generated Error DTO, the concrete RestClient adapter for that client
     *  may override onStatus handling to:
     *  - parse the downstream Error DTO for logging/diagnostics
     *  - throw a typed exception that carries the parsed DTO (recommended for 5xx so CB learns)
     *  - optionally map certain 4xx responses into an explicit domain outcome when they represent expected behavior
     *  Do this only when concrete use cases exist. Avoid a generic "domain model has an error field" by default.
     */
    public static RestClient.ResponseSpec applyDefaultOnStatusHandlers(
            RestClient.ResponseSpec spec,
            String clientName
    ) {
        return spec
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (_, _) -> {
                            throw new DependencyCallException(clientName, Reason.RESPONSE_CLIENT_ERROR);
                        }
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (_, _) -> {
                            throw new DependencyCallException(clientName, Reason.RESPONSE_SERVER_ERROR);
                        }
                );
    }
}
