package com.idear.backend.blockchain.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationStatus {
    SUCCESS("success"),
    FAILURE("failure");

    @JsonValue
    private final String value;

    @JsonCreator
    public static RegistrationStatus fromString(String value) {
        for (RegistrationStatus type : RegistrationStatus.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw CustomException.of(ErrorCode.REGISTRATION_STATUS_NOT_VALID);
    }
}
