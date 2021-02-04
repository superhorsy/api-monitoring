package com.superhorsy.apimonitoring.entities;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RequestData {
    private final Long timestamp;
    private final String host;
    private final String method;
    private final String uri;
    private final Integer statusCode;
    private final String contentType;
    private final Double requestTime;
}
