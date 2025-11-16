package com.idear.backend.global;

import com.idear.backend.global.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";

    private final String status;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(String status, String code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // SUCCESS
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(STATUS_SUCCESS, "200", "OK", null);
    }
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(STATUS_SUCCESS, "200", "OK", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(STATUS_SUCCESS, "200", message, data);
    }

    // ERROR
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(
                STATUS_ERROR,
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(
                STATUS_ERROR,
                errorCode.getCode(),
                errorCode.getMessage(),
                data
        );
    }
}
