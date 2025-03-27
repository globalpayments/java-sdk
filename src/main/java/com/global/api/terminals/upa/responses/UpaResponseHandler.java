package com.global.api.terminals.upa.responses;

import com.global.api.entities.UpaConfigContent;
import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.TerminalResponse;
import com.global.api.utils.JsonDoc;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;

@Getter @Setter
public class UpaResponseHandler extends TerminalResponse {
    public static final String RESPONSE_ID = "responseId";
    public static final String RESPONSE_DATE_TIME = "respDateTime";
    public static final String GATEWAY_RESPONSE_CODE = "gatewayResponseCode";
    public static final String GATEWAY_RESPONSE_MESSAGE = "gatewayResponseMessage";
    public static final String AUTHORIZED_AMOUNT = "authorizedAmount";
    public static final String TRANSACTION_DESCRIPTOR = "txnDescriptor";
    public static final String RECURRING_DATA_CODE = "recurringDataCode";
    public static final String CVV_RESPONSE_CODE = "cvvResponseCode";
    public static final String CVV_RESPONSE_TEXT = "cvvResponseText";
    public static final String CAVV_RESULT_CODE = "CavvResultCode";
    public static final String TRACE_NUMBER = "traceNumber";
    public static final String TOKEN_RESPONSE_CODE = "tokenRspCode";
    public static final String TOKEN_RESPONSE_MESSAGE = "tokenRspMessage";
    public static final String CUSTOM_HASH = "customHash";
    public static final String TAC_DEFAULT = "TacDefault";
    public static final String TAC_DENIAL = "TacDenial";
    public static final String TAC_ONLINE = "TacOnline";
    public static final String DCC = "dcc";
    public static final String EXCHANGE_RATE = "exchangeRate";
    public static final String MARK_UP = "markUp";
    public static final String TRANSACTION_CURRENCY = "transactionCurrency";
    public static final String TRANSACTION_AMOUNT = "transactionAmount";
    public static final String FALLBACK = "fallback";
    public static final String EXPIRY_DATE = "expiryDate";
    public static final String SERVICE_CODE = "serviceCode";
    public static final String DEVICE_SERIAL_NUM = "DeviceSerialNum";
    public static final String APP_VERSION = "AppVersion";
    public String OS_VERSION = "OsVersion";
    public String EMV_SDK_VERSION = "EmvSdkVersion";
    public String CTLS_SDK_VERSION = "CTLSSsdkVersion";

    private BigDecimal availableBalance;
    private String transactionId;
    private String terminalRefNumber;
    private String token;
    private String tokenResponseCode;
    private String tokenResponseMessage;
    private String cardBrandTransId;
    private String signatureStatus;
    private byte[] signatureData;
    private String transactionType;
    private String maskedCardNumber;
    private String entryMethod;
    private String authorizationCode;
    private BigDecimal transactionAmount;
    private BigDecimal amountDue;
    private BigDecimal balanceAmount;
    private String cardBIN;
    private boolean cardPresent;
    private String expirationDate;
    private String avsResponseCode;
    private String avsResponseText;
    private String cvvResponseCode;
    private String cvvResponseText;
    private boolean taxExempt;
    private String taxExemptId;
    private String ticketNumber;
    private String paymentType;
    private String applicationPreferredName;
    private String applicationLabel;
    private String applicationId;
    private ApplicationCryptogramType applicationCryptogramType;
    private String applicationCryptogram;
    private String cardHolderVerificationMethod;
    private String terminalVerificationResults;
    private BigDecimal merchantFee;
    private String status;
    private String command;
    private String version;
    private String deviceResponseCode;
    private String deviceResponseText;
    private String responseCode;
    private String responseText;
    private String approvalCode;
    private BigDecimal tipAmount;
    private BigDecimal baseAmount;
    private BigDecimal cashBackAmount;
    private String referenceNumber;
    private String cardHolderName;
    private String deviceSerialNum;
    private String appVersion;
    private String osVersion;
    private String emvSdkVersion;
    private String ctlssdkVersion;
    private String requestId;
    private String scanData;
    private String dataString;
    private UpaConfigContent ConfigContent;
    private String responseDateTime;
    private String gatewayResponseCode;
    private String gatewayResponsemessage;
    private double additionalTipAmount;
    private double taxAmount;
    private String cpcInd;
    private String descriptor;
    private int tokenPANLast;
    private int partialApproval;
    private String traceNumber;
    private double baseDue;
    private double taxDue;
    private double tipDue;
    private String customHash;
    private String cardGroup;
    private String signatureLine;
    private String qpsQualified;
    private int clerkId;
    private String invoiceNumber;

    public static final String INVALID_RESPONSE_FORMAT = "The response received is not in the proper format.";

    public void parseResponse(JsonDoc root) throws ApiException {
        JsonDoc response;
        if (!isGpApiResponse(root)) {
            response = root.get("data");
            if (response == null) {
                return;
            }
            requestId = response.getString("requestId");
            command = response.getString("response");
        } else {
            response = root.get("response");
            status = root.getStringOrNull("status");
            transactionId = requestId = root.getStringOrNull("id");
            responseText = root.get("action").getStringOrNull("result_code");
        }
        hydrateCmdResult(response);
    }

    protected static boolean isGpApiResponse(JsonDoc root) {
        return !root.has("data");
    }

    protected void hydrateCmdResult(JsonDoc response) throws ApiException {
        JsonDoc cmdResult = response.get("cmdResult");
        if (cmdResult == null) {
            throw new ApiException(INVALID_RESPONSE_FORMAT);
        }
        if (status == null) {
            status = cmdResult.getString("result");
        }
        String[] successStatusList = {"Success", "COMPLETE"};
        deviceResponseCode = Arrays.asList(successStatusList).contains(status) ? "00" : cmdResult.getString("errorCode");
        deviceResponseText = deviceResponseCode.equals("00") ? status : cmdResult.getString("errorMessage");
    }
}