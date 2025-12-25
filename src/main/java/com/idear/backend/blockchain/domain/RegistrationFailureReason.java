package com.idear.backend.blockchain.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.idear.backend.global.exception.CustomException;
import com.idear.backend.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RegistrationFailureReason {
    ALREADY_REGISTERED("already_registered"),
    SUBMISSION_FAILED("submission_failed"),
    NETWORK_ERROR("network_error"),
    RPC_RATE_LIMIT("rpc_rate_limit");

    @JsonValue
    private final String reason;

    @JsonCreator
    public static RegistrationFailureReason fromString(String value) {
        for (RegistrationFailureReason type : RegistrationFailureReason.values()) {
            if (type.reason.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw CustomException.of(ErrorCode.REGISTRATION_FAILURE_REASON_NOT_VALID);
    }
}
