package com.idear.backend.blockchain.dto.request;

import com.idear.backend.blockchain.domain.RegistrationFailureReason;
import com.idear.backend.blockchain.domain.RegistrationStatus;
import lombok.Getter;

@Getter
public class RegistrationResultRequest {

    private RegistrationStatus status;
    private String commit;
    private String txHash;

    private SuccessData successData;
    private FailureData failureData;

    @Getter
    public static class SuccessData {
        private Integer blockNumber;
        private Long registeredAt;
        private String gasUsed;
        private String gasPrice;
        private String gasCostEth;
    }

    @Getter
    public static class FailureData {
        private RegistrationFailureReason reason;
        private String error;
    }
}