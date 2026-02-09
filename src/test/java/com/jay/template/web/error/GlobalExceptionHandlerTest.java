package com.jay.template.web.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.jay.template.api.v1.common.error.ErrorResponse;
import com.jay.template.core.error.api.ApiException;
import com.jay.template.web.mvc.error.GlobalExceptionHandler;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.jay.template.core.error.api.ErrorType.INTERNAL_SERVER_ERROR;
import static com.jay.template.core.error.api.ErrorType.UNAUTHORIZED;

class GlobalExceptionHandlerTest {

    @Test
    void handlesApiException() {
        ApiException ex = mock(ApiException.class);
        when(ex.type()).thenReturn(UNAUTHORIZED);

        ErrorResponseSpecFactory errorResponseSpecFactory = mock(ErrorResponseSpecFactory.class);
        ErrorResponseSpec spec = mock(ErrorResponseSpec.class);

        when(errorResponseSpecFactory.buildResponseSpec(any())).thenReturn(spec);
        when(spec.status()).thenReturn(HttpStatus.BAD_REQUEST);
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(spec.body()).thenReturn(errorResponse);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(errorResponseSpecFactory);
        ResponseEntity<ErrorResponse> entity = handler.handleApiException(ex);

        verify(errorResponseSpecFactory).buildResponseSpec(UNAUTHORIZED);

        assertSame(HttpStatus.BAD_REQUEST, entity.getStatusCode());
        assertSame(errorResponse, entity.getBody());
    }

    @Test
    void handlesGenericException() {
        Exception ex = mock(Exception.class);

        ErrorResponseSpecFactory errorResponseSpecFactory = mock(ErrorResponseSpecFactory.class);
        ErrorResponseSpec spec = mock(ErrorResponseSpec.class);

        when(errorResponseSpecFactory.buildResponseSpec(any())).thenReturn(spec);
        when(spec.status()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse errorResponse = mock(ErrorResponse.class);
        when(spec.body()).thenReturn(errorResponse);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(errorResponseSpecFactory);
        ResponseEntity<ErrorResponse> entity = handler.handleGenericException(ex);

        verify(errorResponseSpecFactory).buildResponseSpec(INTERNAL_SERVER_ERROR);

        assertSame(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
        assertSame(errorResponse, entity.getBody());
    }


}