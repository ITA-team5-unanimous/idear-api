package com.idear.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EXAMPLE_ERROR(HttpStatus.BAD_REQUEST, "C000", "잘못된 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
