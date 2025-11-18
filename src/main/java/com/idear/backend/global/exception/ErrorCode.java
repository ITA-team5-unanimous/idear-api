package com.idear.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "G002", "유효하지 않은 입력입니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A002", "접근 권한이 없습니다."),
    EXPIRED_TOKEN(HttpStatus.BAD_REQUEST, "A003", "이미 만료된 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 엑세스 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A005", "유효하지 않은 리프레시 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 유저입니다."),
    USER_DELETED(HttpStatus.NOT_FOUND, "U002", "탈퇴 처리된 유저입니다."),

    // Idea
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드를 실패했습니다."),
    FILE_NOTFOUND_ERROR(HttpStatus.NOT_FOUND, "F002", "존재하지 않는 파일입니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F003", "파일 삭제를 실패했습니다."),
    IDEA_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "존재하지 않는 아이디어 입니다."),

    // Crawling
    CRAWLING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "크롤링 중 오류가 발생했습니다."),
    PAGE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "페이지 파싱에 실패했습니다."),
    CONTEST_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "공모전 저장에 실패했습니다."),
    CONTEST_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "존재하지 않는 공모전입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
