package com.jay.template.web.error;

import org.springframework.http.HttpStatus;

import com.jay.template.api.v1.common.error.ErrorResponse;

//used to wrap error response contract with protocol layer response concerns
public record ErrorResponseSpec(HttpStatus status, ErrorResponse body) {}
