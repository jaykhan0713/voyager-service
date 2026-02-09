package com.jay.template.smoke;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.infra.outbound.http.client.rest.adapter.ping.contract.DownstreamPingResponse;
import com.jay.template.web.mvc.controller.smoke.api.model.SmokeResponse;
import com.jay.template.common.FunctionalTestBase;
import com.jay.template.common.SpringBootTestShared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.MediaType.APPLICATION_JSON;

@SpringBootTestShared
class SmokeApiTest extends FunctionalTestBase {

    private final TestRestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    SmokeApiTest(
            TestRestTemplate restTemplate,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = restTemplate; //automatically configured to talk to spring instance url
        this.objectMapper = objectMapper;
    }

    /*
     * Parameterized test to make sure
     * [0]. If the app is passed a valid traceparent in headers, app doesn't generate a new traceid
     * [1]. If the app is not passed a traceparent in headers, app does generate a new traceid.
     */
    private static Stream<Arguments> existingTraceParentParams() {
        return Stream.of(
                Arguments.of(
                        "00-9f753cb0b1c2a1ae7c8ebb2a3af249f8-272396ce796067e8-01"
                ),
                Arguments.of("")
        );
    }

    @ParameterizedTest
    @MethodSource("existingTraceParentParams")
    void smokeCallsDownstreamAndPropagatesTraceAndIdentityHeaders(
            String existingTraceParentOrEmpty
    ) throws Exception {
        //the mocked response that the downstream mock server responds with
        DownstreamPingResponse downstreamResponse = new DownstreamPingResponse("pong");

        mockWebServer.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON)
                .setBody(objectMapper.writeValueAsString(downstreamResponse)));

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-user-id", "smoke-user-001");
        headers.set("x-request-id", "smoke-request-001");

        if (!existingTraceParentOrEmpty.isBlank()) {
            headers.set("traceparent", existingTraceParentOrEmpty);
        }


        HttpEntity<Void> entity = new HttpEntity<>(headers);

        //request the app server
        ResponseEntity<SmokeResponse> response =
                restTemplate.exchange(
                        "/api/smoke",
                        HttpMethod.GET,
                        entity,
                        SmokeResponse.class
                );

        SmokeResponse body = response.getBody();

        assertNotNull(body);
        var pingData = body.pingData();
        assertEquals("ping", pingData.businessPath());
        assertTrue(pingData.success());
        assertEquals("pong", pingData.message());

        //the request that went to downstream mock server from spring app
        RecordedRequest pingServerRequest = mockWebServer.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(pingServerRequest, "Downstream did not receive request");

        assertEquals("GET", pingServerRequest.getMethod());
        assertEquals("/api/v1/ping", pingServerRequest.getPath());

        String traceparent = pingServerRequest.getHeader("traceparent");
        assertNotNull(traceparent, "Missing traceparent header on ping request");

        if (!existingTraceParentOrEmpty.isBlank()) {
            String existingTraceId = existingTraceParentOrEmpty.split("-")[1];
            String traceId = traceparent.split("-")[1];

            assertEquals(existingTraceId, traceId);

            String existingSpanId = existingTraceParentOrEmpty.split("-")[2];
            String spanId = traceparent.split("-")[2];

            assertNotEquals(existingSpanId, spanId);
        }

        String userId = pingServerRequest.getHeader("x-user-id");
        assertEquals("smoke-user-001", userId);

        String requestId = pingServerRequest.getHeader("x-request-id");
        assertEquals("smoke-request-001", requestId);
    }
}
