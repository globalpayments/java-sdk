package com.global.api.terminals;

import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.entities.enums.CardType;
import com.global.api.terminals.abstractions.IDeviceResponse;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public abstract class TerminalResponse implements IDeviceResponse {
    // Internal
    protected String status;
    protected String command;
    protected String version;

    // Functional
    protected String deviceResponseCode;
    protected String deviceResponseText;
    protected String responseCode;
    protected String responseText;
    protected String transactionId;
    protected String terminalRefNumber;
    protected String token;
    protected String signatureStatus;
    protected byte[] signatureData;
    protected String qpsQualified;
    protected String storeAndForward;

    // Transactional
    protected String transactionType;
    protected String maskedCardNumber;
    protected String entryMethod;
    protected String authorizationCode;
    protected String approvalCode;
    protected BigDecimal transactionAmount;
    protected BigDecimal authorizedAmount;
    protected BigDecimal amountDue;
    protected BigDecimal balanceAmount;
    protected String cardHolderName;

    protected String cardBIN;
    protected boolean cardPresent;
    protected CardType cardType;
    protected String currency;
    protected String expirationDate;
    protected BigDecimal baseAmount;
    protected BigDecimal tipAmount;
    protected BigDecimal cashBackAmount;
    protected String avsResponseCode;
    protected String avsResponseText;
    protected String cvvResponseCode;
    protected String cvvResponseText;
    protected String avsResultCode;
    protected String avsResultText;
    protected String cvvResultCode;
    protected String cvvResultText;
    protected boolean taxExempt;
    protected String taxExemptId;
    protected String ticketNumber;
    protected String paymentType;
    protected BigDecimal merchantFee;
    protected String merchantId;
    protected String cardBrandTransactionId;
    protected String expiryDate;
    protected BigDecimal serviceCode;
    protected String cpcInd;

    // EMV
    protected String applicationPreferredName;
    protected String applicationLabel;
    protected String applicationId;
    protected ApplicationCryptogramType applicationCryptogramType;
    protected String applicationCryptogram;
    protected String customerVerificationMethod;
    protected String terminalVerificationResults;
    protected String errorCode;
    protected String unmaskedCardNumber;
    protected String applicationIdentifier;
    protected String transactionCurrencyCode;
    protected String sequenceNo;
    protected String posSequenceNbr;
    protected String applicationInterchangeProfile;
    protected String dedicatedFileName;
    protected String authorizedResponse;
    protected String terminalVerificationResult;
    protected String terminalTransactionQualifiers;
    protected String transactionPin;
    protected String transactionDate;
    protected String transactionStatusInfo;
    protected String emvTransactionType;
    protected String amountAuthorized;
    protected String otherAmount;
    protected String applicationVersionNumber;
    protected String issuerActionCode;
    protected String iacDenial;
    protected String iacOnline;
    protected String issuerApplicationData;
    protected String countryCode;
    protected String serialNo;
    protected Integer cryptogramInfoData;
    protected String terminalCapabilities;
    protected String cvmResult;
    protected String terminalType;
    protected String applicationTransactionCounter;
    protected String unpredictableNumber;
    protected String additionalTerminalCapabilities;
    protected String transactionSequenceCounter;
    protected String tacDefault;
    protected String tacDenial;
    protected String tacOnline;

    //Host
    protected String responsesId;
    protected String responseDateTime;
    protected Integer gatewayResponsCode;
    protected String gatewayResponseMessage;
    protected BigDecimal authorizeAmount;
    protected String transactionDescriptor;
    protected String recurringDataCode;
    protected String cavvResultCode;
    protected Integer traceNo;
    protected String tokenResponsCode;
    protected String traceNumber;
    protected String tokenResponseMessage;
    protected String customHash;

    //DCC
    protected BigDecimal exchangeRate;
    protected BigDecimal markUp;
    protected String transactionCurrency;
    protected String deviceSerialNum;
    protected String appVersion;
    protected String osVersion;
    protected String emvSdkVersion;
    protected String ctlsSdkVersion;
    protected String requestId;
    protected String originalTransactionType;

    //fallback
    protected Integer fallback;
    protected String pinVerified;
    protected String accountType;
    protected String issuerResponseCode;
    protected String isoResponseCode;
    protected String bankResponseCode;
    protected String applicationName;
    protected String cardHolderLanguage;
    protected String terminalStatusIndicator;
}