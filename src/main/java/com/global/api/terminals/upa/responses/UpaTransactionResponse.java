package com.global.api.terminals.upa.responses;

import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.entities.enums.CardType;
import com.global.api.terminals.TerminalResponse;
import com.global.api.utils.JsonDoc;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Locale;

public class UpaTransactionResponse extends TerminalResponse {
    public static final String RESPONSE_ID = "responseId";
    public static final String RESPONSE_DATE_TIME = "respDateTime";
    public static final String GATEWAY_RESPONSE_CODE = "gatewayResponseCode";
    public static final String GATEWAY_RESPONSE_MESSAGE = "gatewayResponseMessage";
    private static final String RESPONSE_CODE = "responseCode";
    private static final String RESPONSE_TEXT = "responseText";
    private static final String TRAN_NO = "tranNo";
    private static final String APPROVAL_CODE = "approvalCode";
    private static final String REFERENCE_NUMBER = "referenceNumber";
    private static final String AVS_RESULT_CODE = "AvsResultCode";
    private static final String AVS_RESULT_TEXT = "AvsResultText";
    private static final String CVV_RESULT_CODE = "cvvResultCode";
    private static final String CVV_RESULT_TEXT = "cvvResultText";
    private static final String BASE_AMOUNT = "baseAmount";
    private static final String TOTAL_AMOUNT = "totalAmount";
    public static final String AUTHORIZED_AMOUNT = "authorizedAmount";
    private static final String CPC_IND = "CpcInd";
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
    private static final String SIGNATURE_LINE = "signatureLine";
    private static final String QPS_QUALIFIED = "QpsQualified";
    private static final String STORE_AND_FORWARD = "storeAndForward";
    private static final String RESPONSE = "response";

    private BigDecimal totalAmount;
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public UpaTransactionResponse(JsonDoc responseData) {
        JsonDoc response;
        JsonDoc cmdResult;

        if (isGpApiResponse(responseData)) {
            status = responseData.getStringOrNull("status");
            transactionId = requestId = responseData.getStringOrNull("id");
            responseText = responseData.get("action").getStringOrNull("result_code");
            if (status.equalsIgnoreCase("COMPLETE")) deviceResponseCode = "00";
            response = responseData.get("response");
        } else {
            response = responseData.get("data");
            if (response == null) {
                return;
            }
        }
        deviceResponseText = responseData.getStringOrNull("status");

        cmdResult = response.get("cmdResult");

        if (cmdResult != null) {
            status = cmdResult.getString("result");
            if (status.equalsIgnoreCase("success")) {
                deviceResponseCode = "00";
            } else {
                deviceResponseCode = cmdResult.getString("errorCode");
                deviceResponseText = cmdResult.getString("errorMessage");
            }
        }

        // ToDo: is this needed here?
//            transactionType = responseData.getString("response");

        JsonDoc data = response.get("data");
        if (data == null) {
            return;
        }

        JsonDoc host = data.get("host");

        if (host != null) {
            if (host.has(RESPONSE_ID)) {
                responsesId = host.getString(RESPONSE_ID);
            }
            if (host.has(RESPONSE_DATE_TIME)) {
                responseDateTime = host.getString(RESPONSE_DATE_TIME);
            }
            if (host.has(GATEWAY_RESPONSE_CODE)) {
                gatewayResponsCode = host.getInt(GATEWAY_RESPONSE_CODE);
            }
            if (host.has(GATEWAY_RESPONSE_MESSAGE)) {
                gatewayResponseMessage = host.getString(GATEWAY_RESPONSE_MESSAGE);
            }
            if (host.has(RESPONSE_CODE)) {
                responseCode = host.getString(RESPONSE_CODE);
            }
            if (host.has(RESPONSE_TEXT)) {
                responseText = host.getString(RESPONSE_TEXT);
            }
            if (host.has(TRAN_NO)) {
                terminalRefNumber = host.getString(TRAN_NO);
            }
            if (host.has(APPROVAL_CODE)) {
                approvalCode = host.getString(APPROVAL_CODE);
            }
            if (host.has(REFERENCE_NUMBER)) {
                transactionId = host.getString(REFERENCE_NUMBER);
            }
            if (host.has(AVS_RESULT_CODE)) {
                avsResultCode = host.getString(AVS_RESULT_CODE);
            }
            if (host.has(AVS_RESULT_TEXT)) {
                avsResultText = host.getString(AVS_RESULT_TEXT);
            }
            if (host.has(CVV_RESULT_CODE)) {
                cvvResultCode = host.getString(CVV_RESULT_CODE);
            }
            if (host.has(CVV_RESULT_TEXT)) {
                cvvResultText = host.getString(CVV_RESULT_TEXT);
            }
            if (host.has(BASE_AMOUNT)) {
                transactionAmount = host.getDecimal(BASE_AMOUNT);
            }
            if (host.has(TOTAL_AMOUNT)) {
                totalAmount = host.getDecimal(TOTAL_AMOUNT);
            }
            if (host.has(AUTHORIZED_AMOUNT)) {
                authorizedAmount = host.getDecimal(AUTHORIZED_AMOUNT);
            }
            if (host.has(CPC_IND)) {
                cpcInd = host.getString(CPC_IND);
            }
            amountDue = host.getDecimal("balanceDue");
            balanceAmount = host.getDecimal("availableBalance");
            cardBrandTransactionId = host.getString("cardBrandTransId");
            merchantFee = host.getDecimal("surcharge");
            token = host.getString("tokenValue");
            issuerResponseCode = host.getString("IssuerResp");
            isoResponseCode = host.getString("IsoRespCode");
            bankResponseCode = host.getString("BankRespCode");

            if (transactionAmount == null) {
                BigDecimal amount = host.getDecimal("amount");
                if (amount != null) {
                    transactionAmount = amount;
                }
            }
            transactionDescriptor = host.getString(TRANSACTION_DESCRIPTOR);
            if (host.has(RECURRING_DATA_CODE)) {
                recurringDataCode = host.getString(RECURRING_DATA_CODE);
            }
            cvvResponseCode = host.getString(CVV_RESPONSE_CODE);
            cvvResponseText = host.getString(CVV_RESPONSE_TEXT);
            if (host.has(CAVV_RESULT_CODE)) {
                cavvResultCode = host.getString(CAVV_RESULT_CODE);
            }

            if (host.has(TRACE_NUMBER)) {
                traceNumber = host.getString(TRACE_NUMBER);
            }
            if (host.has(TOKEN_RESPONSE_CODE)) {
                tokenResponsCode = host.getString(TOKEN_RESPONSE_CODE);
            }
            tokenResponseMessage = host.getString(TOKEN_RESPONSE_MESSAGE);
            customHash = host.getString(CUSTOM_HASH);
        }

        JsonDoc payment = data.get("payment");

        if (payment != null) { // is null on decline response
            cardHolderName = payment.getString("cardHolderName");

            if (payment.has("cardType")) {
                switch (payment.getString("cardType").toUpperCase(Locale.ENGLISH)) {
                    case "VISA":
                        cardType = CardType.VISA;
                        break;
                    case "MASTERCARD":
                        cardType = CardType.MC;
                        break;
                    case "DISCOVER":
                        cardType = CardType.DISC;
                        break;
                    case "AMERICAN EXPRESS":
                        cardType = CardType.AMEX;
                        break;
                    default:
                        break;
                }
            }

            entryMethod = payment.getString("cardAcquisition");
            maskedCardNumber = payment.getString("maskedPan");
            paymentType = payment.getString("cardGroup");

            pinVerified = payment.getString("PinVerified");
            accountType = payment.getString("AccountType");
            transactionType = payment.getString("transactionType");
            sequenceNo = payment.getString("PosSequenceNbr");
            applicationName = payment.getString("appName");

            //Added Fallback for startCardTransaction
            if (payment.has(FALLBACK)) {
                fallback = payment.getInt(FALLBACK);
            }

            //Added Expiry for startCardTransaction
            if (payment.has(EXPIRY_DATE)) {
                expirationDate = payment.getString(EXPIRY_DATE);
            }

            if (payment.has(SIGNATURE_LINE)) {
                signatureStatus = payment.getString(SIGNATURE_LINE);
            }

            if (payment.has(QPS_QUALIFIED)) {
                qpsQualified = payment.getString(QPS_QUALIFIED);
            }

            if (payment.has(STORE_AND_FORWARD)) {
                storeAndForward = payment.getString(STORE_AND_FORWARD);
            }
        }

        if (data.has(RESPONSE)) {
            command = data.getString(RESPONSE);
        }

        JsonDoc transaction = data.get("transaction");

        if (transaction != null) {

            if (transaction.getDecimal("totalAmount") != null) {

                transactionAmount = transaction.getDecimal("totalAmount");

            }
            if (transaction.getDecimal("tipAmount") != null) {
                tipAmount = transaction.getDecimal("tipAmount");
            }
        }

        JsonDoc emv = data.get("emv");

        if (emv != null) {
            applicationCryptogram = emv.getString("9F26");

            if (emv.getString("9F27") != null) {
                switch (emv.getString("9F27")) {
                    case "0":
                        applicationCryptogramType = ApplicationCryptogramType.AAC;
                        break;
                    case "40":
                        applicationCryptogramType = ApplicationCryptogramType.TC;
                        break;
                    case "80":
                        applicationCryptogramType = ApplicationCryptogramType.ARQC;
                        break;
                    default:
                        break;
                }
            }
            terminalStatusIndicator = emv.getString("9B");
            applicationId = emv.getString("9F06");
            applicationLabel = emv.getString("50");
            applicationPreferredName = emv.getString("9F12");
            applicationIdentifier = emv.getString("4F");
            cardHolderName = emv.getString("5F20");
            if (emv.getString("5F2A") != null) {
                transactionCurrencyCode = emv.getString("5F2A");
            }
            if (emv.getString("5F2D") != null) {
                cardHolderLanguage = emv.getString("5F2D");
            }
            if (emv.getString("5F34") != null) {
                sequenceNo = emv.getString("5F34");
            }
            applicationInterchangeProfile = emv.getString("82");
            dedicatedFileName = emv.getString("84");
            authorizedResponse = emv.getString("8A");
            if (emv.getString("95") != null) {
                terminalVerificationResult = emv.getString("95");
            }
            if (emv.getString("99") != null) {
                transactionPin = emv.getString("99");
            }
            transactionDate = emv.getString("9A");
            if (emv.getString("9B") != null) {
                transactionStatusInfo = emv.getString("9B");
            }
            if (emv.getString("9C") != null) {
                emvTransactionType = emv.getString("9C");
            }
            if (emv.getString("9F02") != null) {
                amountAuthorized = emv.getString("9F02");
            }
            if (emv.getString("9F03") != null) {
                otherAmount = emv.getString("9F03");
            }
            if (emv.getString("9F08") != null) {
                applicationVersionNumber = emv.getString("9F08");
            }
            issuerActionCode = emv.getString("9F0D");
            iacDenial = emv.getString("9F0E");
            iacOnline = emv.getString("9F0F");
            if (emv.getString("9F10") != null) {
                issuerApplicationData = emv.getString("9F10");
            }
            if (emv.getString("9F1A") != null) {
                countryCode = emv.getString("9F1A");
            }
            serialNo = emv.getString("9F1E");
            terminalCapabilities = emv.getString("9F33");
            cvmResult = emv.getString("9F34");
            if (emv.getString("9F35") != null) {
                terminalType = emv.getString("9F35");
            }
            applicationTransactionCounter = emv.getString("9F36");
            unpredictableNumber = emv.getString("9F37");
            additionalTerminalCapabilities = emv.getString("9F40");
            if (emv.getString("9F41") != null) {
                transactionSequenceCounter = emv.getString("9F41");
            }
            tacDefault = emv.getString(TAC_DEFAULT);
            tacDenial = emv.getString(TAC_DENIAL);
            tacOnline = emv.getString(TAC_ONLINE);

        }

        JsonDoc pan = data.get("PAN");

        if (pan != null) {
            unmaskedCardNumber = pan.getString("clearPAN");
        }

        JsonDoc dcc = data.get(DCC);

        if (dcc != null) {
            if (dcc.getDecimal(EXCHANGE_RATE) != null) {
                exchangeRate = dcc.getDecimal(EXCHANGE_RATE);
            }

            if (dcc.getDecimal(MARK_UP) != null) {
                markUp = dcc.getDecimal(MARK_UP);
            }

            transactionCurrency = dcc.getString(TRANSACTION_CURRENCY);

            if (dcc.getDecimal(TRANSACTION_AMOUNT) != null) {
                transactionAmount = dcc.getDecimal(TRANSACTION_AMOUNT);
            }
        }

        if (data.getDecimal(SERVICE_CODE) != null) {
            serviceCode = data.getDecimal(SERVICE_CODE);
        }


    }

    public static UpaTransactionResponse parseResponse(String rawResponse) {
        JsonDoc response = JsonDoc.parse(rawResponse);
        // TODO: We might have to scope the document down depending on what response we actually get from the message endpoint
        return new UpaTransactionResponse(response);
    }

    private boolean isGpApiResponse(JsonDoc root) {
        if (root.has("data")) {
            return false;
        }
        return true;
    }

}
