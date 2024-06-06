package com.global.api.entities.billing;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter @Setter
public class AuthorizationRecord {
    private String AddToBatchReferenceNumber ;
    private BigDecimal amount ;
    private String authCode ;
    private String authorizationType ;
    private String avsResultCode ;
    private String avsResultText ;
    private String cardEntryMethod ;
    private String cvvResultCode ;
    private String cvvResultText ;
    private String emvApplicationCryptogram ;
    private String emvApplicationCryptogramType ;
    private String emvApplicationID ;
    private String emvApplicationName ;
    private String emvCardholderVerificationMethod ;
    private String emvIssuerResponse ;
    private String emvSignatureRequired ;
    private String gateway ;
    private String gatewayBatchID ;
    private String gatewayDescription ;
    private String maskedAccountNumber ;
    private String maskedRoutingNumber ;
    private String paymentMethod ;
    private int referenceAuthorizationID ;
    private String referenceNumber ;
    private String routingNumber ;
    private int authorizationID ;
    private BigDecimal netAmount ;
    private int originalAuthorizationID ;
}
