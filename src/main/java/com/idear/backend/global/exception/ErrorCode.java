package com.idear.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EXAMPLE_ERROR(HttpStatus.BAD_REQUEST, "C000", "잘못된 요청입니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드를 실패했습니다."), 
    FILE_NOTFOUND_ERROR(HttpStatus.NOT_FOUND, "F002", "존재하지 않는 파일입니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "파일 삭제를 실패했습니다.")
    ;


    private final HttpStatus status;
    private final String code;
    private final String message;
}
