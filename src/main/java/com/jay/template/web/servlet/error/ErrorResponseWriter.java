package com.jay.template.web.servlet.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import com.jay.template.core.error.api.ErrorType;
import com.jay.template.web.error.ErrorResponseSpec;
import com.jay.template.web.error.ErrorResponseSpecFactory;

// for servlet/ mapping
public class ErrorResponseWriter {

    private final ErrorResponseSpecFactory errorResponseSpecFactory;
    private final ObjectMapper objectMapper;

    public ErrorResponseWriter(
            ErrorResponseSpecFactory errorResponseSpecFactory,
            ObjectMapper objectMapper
    ) {
        this.errorResponseSpecFactory = errorResponseSpecFactory;
        this.objectMapper = objectMapper;
    }

    public void writeJsonErrorResponse(HttpServletResponse response, ErrorType type) throws IOException {

        // defensive safeguard should never happen due to filter-ordering short circuit
        if (response.isCommitted()) {
            return;
        }

        ErrorResponseSpec spec = errorResponseSpecFactory.buildResponseSpec(type);

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(spec.status().value());

        objectMapper.writeValue(response.getOutputStream(), spec.body());
    }
}
