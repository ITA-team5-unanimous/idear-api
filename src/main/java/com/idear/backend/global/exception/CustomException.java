package com.idear.backend.global.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

  private final ErrorCode errorCode;

  private CustomException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  private CustomException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public static CustomException of(ErrorCode errorCode){
    return new CustomException(errorCode);
  }

  public static CustomException of(ErrorCode errorCode, String message){
    return new CustomException(errorCode, message);
  }
}
