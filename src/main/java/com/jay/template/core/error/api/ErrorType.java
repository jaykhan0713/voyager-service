package com.jay.template.core.error.api;

public enum ErrorType {
    //Server errors
    INTERNAL_SERVER_ERROR("Internal Server Error"),
    DEPENDENCY_UNAVAILABLE( "Dependency Unavailable"),

    //Client errors
    BAD_REQUEST("Bad Request"),
    UNAUTHORIZED( "Unauthorized"), //never used as it's guaranteed by api gateway + cognito
    TOO_MANY_REQUESTS("Too many requests");


    private final String defaultMessage; //these messages are for human readability of the consumer

    ErrorType(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
