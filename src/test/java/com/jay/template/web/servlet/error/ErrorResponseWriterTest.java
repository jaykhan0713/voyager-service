package com.jay.template.web.servlet.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.core.error.api.ErrorType;
import com.jay.template.web.error.ErrorResponseSpec;
import com.jay.template.web.error.ErrorResponseSpecFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static com.jay.template.core.error.api.ErrorType.TOO_MANY_REQUESTS;

class ErrorResponseWriterTest {

    @Test
    void writeErrorResponseWriterTest() throws IOException {
        ErrorResponseSpecFactory factory = mock(ErrorResponseSpecFactory.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ErrorResponseWriter writer = new ErrorResponseWriter(factory, objectMapper);

        ErrorType type = TOO_MANY_REQUESTS;
        String correlationId = "trace-001";
        ErrorResponse body = new ErrorResponse(type.name(), type.defaultMessage(), correlationId);

        ErrorResponseSpec spec = new ErrorResponseSpec(HttpStatus.TOO_MANY_REQUESTS, body);

        when(factory.buildResponseSpec(type)).thenReturn(spec);

        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.writeJsonErrorResponse(response, type);

        verify(factory).buildResponseSpec(type);
        // Verify the writer passes the response output stream and the response body to Jackson
        verify(objectMapper).writeValue(response.getOutputStream(), body);

        MediaType contentType = MediaType.parseMediaType(response.getContentType());
        MediaType expected = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

        assertEquals(expected, contentType);
        assertEquals(spec.status().value(), response.getStatus());
    }

    @Test
    void whenResponseIsCommittedReturns() throws IOException {
        ErrorResponseSpecFactory factory = mock(ErrorResponseSpecFactory.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        ErrorResponseWriter writer = new ErrorResponseWriter(factory, objectMapper);

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);

        writer.writeJsonErrorResponse(response, TOO_MANY_REQUESTS);

        verifyNoInteractions(factory, objectMapper);
    }
}