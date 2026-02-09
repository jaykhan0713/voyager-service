package com.jay.template.core.error.dependency;

public enum Reason {

    UNKNOWN,

    //IO related
    IO_ERROR,

    //status code in response related
    RESPONSE_CLIENT_ERROR, //4xx
    RESPONSE_SERVER_ERROR, //5xx

    //Resiliency related
    CAPACITY_REJECTED,
    SHORT_CIRCUITED
}
