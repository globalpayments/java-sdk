package com.global.api.entities.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
public class GatewayException extends ApiException {
    private final String responseCode;
    private final String responseText;
    private String issuerResponseCode;
    private String issuerResponseText;
    private String deviceResponseCode;
    private String deviceResponseText;

    @Setter private String host;
    @Setter private String messageTypeIndicator;
    @Setter private String posDataCode;
    @Setter private String processingCode;
    @Setter private String transactionToken;
    @Setter private String transmissionTime;
    @Setter private int reversalCount = 0;
    @Setter private String reversalResponseCode;
    @Setter private String reversalResponseText;
    @Setter private  String gatewayTransactionID;

    public GatewayException(String message) {
        this(message, null, null, null, null, null, null, null);
    }
    public GatewayException(String message, Exception innerException) {
        this(message, null, null, null, null, null, null, innerException);
    }
    public GatewayException(String message, String responseCode, String responseText) {
        this(message, responseCode, responseText, null, null, null, null, null);
    }
    public GatewayException(String message, String responseCode, String responseText, Exception innerException) {
        this(message, responseCode, responseText, null, null, null, null, innerException);
    }
    public GatewayException(String message, String responseCode, String responseText, String issuerResponseCode, String issuerResponseText, Exception innerException) {
        this(message, responseCode, responseText, issuerResponseCode, issuerResponseText, null, null, innerException);
    }
    public GatewayException(String message, String responseCode, String responseText, String issuerResponseCode, String issuerResponseText, String deviceResponseCode, String deviceResponseText) {
        this(message, responseCode, responseText, issuerResponseCode, issuerResponseText, deviceResponseCode, deviceResponseText, null);
    }
    public GatewayException(String message, String responseCode, String responseText, String issuerResponseCode, String issuerResponseText, String deviceResponseCode, String deviceResponseText, Exception innerException) {
        super(message, innerException);
        this.responseCode = responseCode;
        this.responseText = responseText;
        this.issuerResponseCode = issuerResponseCode;
        this.issuerResponseText = issuerResponseText;
        this.deviceResponseCode = deviceResponseCode;
        this.deviceResponseText = deviceResponseText;
    }
    public GatewayException(String message, String responseCode, String responseText, String gatewayTransactionID) {
        super(message);
        this.responseCode = responseCode;
        this.responseText = responseText;
        this.gatewayTransactionID = gatewayTransactionID;
    }
}
