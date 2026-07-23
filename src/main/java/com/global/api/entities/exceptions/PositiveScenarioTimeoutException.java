package com.global.api.entities.exceptions;

import lombok.Getter;

@Getter
public class PositiveScenarioTimeoutException extends MessageException {
    private final String transactionId;
    private final String terminalRefNumber;

    public PositiveScenarioTimeoutException(String message, String transactionId, String terminalRefNumber, Exception innerException) {
        super(message, innerException);
        this.transactionId = transactionId;
        this.terminalRefNumber = terminalRefNumber;
    }
}
