package com.idear.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EXAMPLE_ERROR(HttpStatus.BAD_REQUEST, "C000", "잘못된 요청입니다."),

    // 크롤링 관련 에러
    CRAWLING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "크롤링 중 오류가 발생했습니다."),
    PAGE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "페이지 파싱에 실패했습니다."),
    CONTEST_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "공모전 저장에 실패했습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;
}
