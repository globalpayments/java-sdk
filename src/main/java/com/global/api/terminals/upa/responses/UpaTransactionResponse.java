package com.global.api.terminals.upa.responses;

import com.global.api.entities.enums.ApplicationCryptogramType;
import com.global.api.entities.enums.CardType;
import com.global.api.terminals.TerminalResponse;
import com.global.api.utils.JsonDoc;

import java.util.Locale;

public class UpaTransactionResponse extends TerminalResponse {
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

    public UpaTransactionResponse(JsonDoc responseData) {
        JsonDoc cmdResult = responseData.get("cmdResult");

        if (cmdResult != null) {
            status = cmdResult.getString("result");
            deviceResponseCode = status.equalsIgnoreCase("success") ? "00" : cmdResult.getString("errorCode");
            deviceResponseText = cmdResult.getString("errorMessage");
        }

        transactionType = responseData.getString("response");

        JsonDoc data = responseData.get("data");

        if (data != null) {
            JsonDoc host = data.get("host");

            if (host != null) {
                amountDue = host.getDecimal("balanceDue");
                approvalCode = host.getString("approvalCode");
                avsResponseCode = host.getString("AvsResultCode");
                avsResponseText = host.getString("AvsResultText");
                balanceAmount = host.getDecimal("availableBalance");
                cardBrandTransactionId = host.getString("cardBrandTransId");
                responseCode = host.getString("responseCode");
                responseText = host.getString("responseText");
                merchantFee = host.getDecimal("surcharge");
                terminalRefNumber = host.getString("tranNo");
                token = host.getString("tokenValue");
                transactionId = host.getString("referenceNumber");
                transactionAmount = host.getDecimal("amount");

                if (host.getString(RESPONSE_ID) != null){
                    responsesId = host.getString(RESPONSE_ID);
                }
                responseDateTime = host.getString(RESPONSE_DATE_TIME);
                if (host.getInt(GATEWAY_RESPONSE_CODE) != null){
                    gatewayResponsCode = host.getInt(GATEWAY_RESPONSE_CODE);
                }
                gatewayResponseMessage = host.getString(GATEWAY_RESPONSE_MESSAGE);
                if (host.getDecimal(AUTHORIZED_AMOUNT) != null){
                    authorizeAmount = host.getDecimal(AUTHORIZED_AMOUNT);
                }
                transactionDescriptor = host.getString(TRANSACTION_DESCRIPTOR);
                if (host.getString(RECURRING_DATA_CODE) != null){
                    recurringDataCode = host.getString(RECURRING_DATA_CODE);
                }
                cvvResponseCode = host.getString(CVV_RESPONSE_CODE);
                cvvResponseText = host.getString(CVV_RESPONSE_TEXT);
                if (host.getString(CAVV_RESULT_CODE) != null){
                    cavvResultCode = host.getString(CAVV_RESULT_CODE);
                }

                if (host.getString(TRACE_NUMBER) != null){
                    traceNumber = host.getString(TRACE_NUMBER);
                }
                if (host.getString(TOKEN_RESPONSE_CODE) != null){
                    tokenResponsCode = host.getString(TOKEN_RESPONSE_CODE);
                }
                tokenResponseMessage = host.getString(TOKEN_RESPONSE_MESSAGE);
                customHash = host.getString(CUSTOM_HASH);

            }

            JsonDoc payment = data.get("payment");

            if (payment != null) { // is null on decline response
                cardHolderName = payment.getString("cardHolderName");

                if (payment.getString("cardType") != null) {
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
            }

            JsonDoc transaction = data.get("transaction");

            if (transaction != null) {

                if (transaction.getDecimal("amount") != null) {
                    transactionAmount = transaction.getDecimal("amount");
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

                applicationId = emv.getString("9F06");
                applicationLabel = emv.getString("50");
                applicationPreferredName = emv.getString("9F12");
                applicationIdentifier = emv.getString("4F");
                cardHolderName = emv.getString("5F20");
                if (emv.getString("5F2A") != null) {
                    transactionCurrencyCode = emv.getString("5F2A");
                }
                if (emv.getString("5F34") != null){
                    sequenceNo = emv.getString("5F34");
                }
                applicationInterchangeProfile = emv.getString("82");
                dedicatedFileName = emv.getString("84");
                authorizedResponse = emv.getString("8A");
                if (emv.getString("95") != null){
                    terminalVerificationResult = emv.getString("95");
                }
                if (emv.getString("99") != null){
                    transactionPin = emv.getString("99");
                }
                transactionDate = emv.getString("9A");
                if (emv.getString("9B") != null){
                    transactionStatusInfo = emv.getString("9B");
                }
                if (emv.getString("9C") != null){
                    emvTransactionType= emv.getString("9C");
                }
                if (emv.getString("9F02") != null){
                    amountAuthorized = emv.getString("9F02");
                }
                if (emv.getString("9F03") != null){
                    otherAmount = emv.getString("9F03");
                }
                if (emv.getString("9F08") != null){
                    applicationVersionNumber = emv.getString("9F08");
                }
                issuerActionCode = emv.getString("9F0D");
                iacDenial = emv.getString("9F0E");
                iacOnline = emv.getString("9F0F");
                if (emv.getString("9F10") != null){
                    issuerApplicationData = emv.getString("9F10");
                }
                if (emv.getString("9F1A") != null){
                    countryCode = emv.getString("9F1A");
                }
                serialNo = emv.getString("9F1E");
                terminalCapabilities = emv.getString("9F33");
                cvmResult = emv.getString("9F34");
                if (emv.getString("9F35") != null){
                    terminalType = emv.getString("9F35");
                }
                applicationTransactionCounter = emv.getString("9F36");
                unpredictableNumber = emv.getString("9F37");
                additionalTerminalCapabilities = emv.getString("9F40");
                if (emv.getString("9F41") != null){
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

                if (dcc.getDecimal(MARK_UP) != null){
                    markUp = dcc.getDecimal(MARK_UP);
                }

                transactionCurrency = dcc.getString(TRANSACTION_CURRENCY);

                if (dcc.getDecimal(TRANSACTION_AMOUNT) != null) {
                    transactionAmount = dcc.getDecimal(TRANSACTION_AMOUNT);
                }
            }
        }
    }
}
