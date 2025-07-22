package com.global.api.entities.exceptions;

import com.global.api.entities.AdditionalDuplicateData;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GatewayDuplicateException extends GatewayException {
    private final AdditionalDuplicateData additionalDuplicateData;


    public GatewayDuplicateException(AdditionalDuplicateData additionalDuplicateData) {
        super("The gateway refused the transaction due to duplicate checking.");
        this.additionalDuplicateData = additionalDuplicateData;
    }
    public GatewayDuplicateException(AdditionalDuplicateData additionalDuplicateData, Exception innerException) {
        super("The gateway refused the transaction due to duplicate checking.", innerException);
        this.additionalDuplicateData = additionalDuplicateData;
    }
    public GatewayDuplicateException(AdditionalDuplicateData additionalDuplicateData, String message, String gatewayRspCode, String gatewayRspText, String gatewayTxnId) {
        super(message, gatewayRspCode, gatewayRspText, gatewayTxnId);
        this.additionalDuplicateData = additionalDuplicateData;
    }
}