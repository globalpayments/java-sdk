package com.global.api.terminals.diamond.responses;

import com.global.api.terminals.TerminalResponse;
import com.global.api.terminals.abstractions.IBatchCloseResponse;
import com.global.api.terminals.abstractions.ITerminalReport;
import com.global.api.terminals.diamond.enums.*;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DiamondCloudResponse extends TerminalResponse implements IBatchCloseResponse, ITerminalReport {
    /**
     * For Visa Contactless cards: Visa available offline spending amount For Erzsebet cards: remaining
     * balance of the Voucher Type used NOTE: should be printed on the customer receipt only, not on a
     * merchant/control receipt.
     **/
    private String aosa;

    /**
     * Authorization message number – usually equal to transaction number.
     **/
    private String authorizationMessage;

    /**
     * Cardholder authorization method, possible values enum class AuthorizationMethod
     **/
    private String authorizationMethod;

    /**
     * Authorization type, possible values enum class AuthorizationType
     **/
    private String authorizationType;

    /**
     * Brand name of the card – application label(EMV)or cardset name
     **/
    private String cardBrandName;

    /**
     * Reader used to read card data. This character depends on the acquirer values in enum class CardSource
     **/
    private String cardSource;

    /**
     * Transaction date in format YYYY.MM.DD
     **/
    private String date;

    /**
     * currencyExchangeRate float Currency exchange rate. Should be set only for DCC transaction.
     * Uses dot ‘.’ as a separator.
     **/
    private BigDecimal currencyExchangeRate;

    /**
     * DCC currency exponent.
     **/
    private Integer dccCurrencyExponent;

    /**
     * DCC text 1. Should be set only for DCC transaction.
     **/
    private String dccText1;

    /**
     * DCC text 2. Should be set only for DCC transaction.
     **/
    private String dccText2;

    /**
     * Optional descriptive information about intent or android specific error
     **/
    private String errorMessage;

    /**
     * Merchant ID
     **/
    private String merchantId;

    /**
     * Transaction number
     **/
    private String clientTransactionId;

    /**
     * Terminal currency
     **/
    private String terminalCurrency;

    /**
     * Terminal identifier
     **/
    private String terminalId;

    /**
     * Terminal printing indicator (value not equal 0 means that printout has been made by the terminal).
     **/
    private String terminalPrintingIndicator;

    /**
     * Transaction time format hh:mm:ss
     **/
    private String time;

    /**
     * Transaction amount in terminal currency. Should be set only for DCC transaction
     **/
    private BigDecimal transactionAmountInTerminalCurrency;

    /**
     * Transaction currency. Should be always set. In DCC transaction this currency is selected by user.
     **/
    private String transactionCurrency;

    /**
     * Transaction title
     **/
    private String transactionTitle;

    /**
     * EMV Application Identifier
     **/
    private String emvApplicationId;//AID

    /**
     * TVR for EMV
     **/
    private String emvTerminalVerificationResults; //TVR

    /**
     * TSI for EMV
     **/
    private String emvCardHolderVerificationMethod; //TSI

    /**
     * EMV Transaction Cryptogram
     **/
    private String emvCryptogram; //AC

    /**
     * EMV card transaction counter
     **/
    private String emvCardTransactionCounter; //ATC

    private String invoiceNumber;

    private String resultId;

    private String batchNumber;

//    private String ResponseCode;

//    private String ResponseText;

//    private String TransactionId;

//    private String TerminalRefNumber;

//    private String Token;

//    private String SignatureStatus;

//    private byte[] SignatureData;

//    private String TransactionType;

//    private String MaskedCardNumber;

//    private String EntryMethod;

//    private String AuthorizationCode;

//    private String ApprovalCode;

//    private BigDecimal TransactionAmount;

//    private BigDecimal AmountDue;

//    private BigDecimal BalanceAmount;

//    private String CardHolderName;

//    private String CardBIN;

//    private boolean CardPresent;

//    private String ExpirationDate;

//    private BigDecimal TipAmount;

//    private BigDecimal CashBackAmount;

//    private String AvsResponseCode;

//    private String AvsResponseText;

//    private String CvvResponseCode;

    //    private String CvvResponseText;
//    private boolean TaxExempt;
//    private String taxExemptId;
//    private String TicketNumber;
//    private String PaymentType;
//    private String ApplicationPreferredName;
//    private String ApplicationLabel;
//    private String ApplicationId;
//    private ApplicationCryptogramType ApplicationCryptogramType;
//    private String ApplicationCryptogram;
    private String cardHolderVerificationMethod;
//    private String TerminalVerificationResults;
//    private BigDecimal MerchantFee;

    private String referenceNumber;

    private String totalAmount;

    private String sequenceNumber;

    private String totalCount;

    public DiamondCloudResponse(String rawResponse) {
        if (JsonDoc.isJson(rawResponse)) {
            JsonDoc parsedJson = JsonDoc.parse(rawResponse);

            JsonDoc paymentDetails = null;
            JsonDoc paymentResponse = parsedJson.get("PaymentResponse");
            if (paymentResponse == null) {
                paymentDetails = null;
            } else {
                JsonDoc innerPaymentResponse = paymentResponse.get("PaymentResponse");
                if (innerPaymentResponse != null) {
                    paymentDetails = innerPaymentResponse;
                } else {
                    paymentDetails = paymentResponse;
                }
            }

            transactionId = parsedJson.getStringOrNull("CloudTxnId");
            invoiceNumber = parsedJson.getStringOrNull("InvoiceId");
            referenceNumber = parsedJson.getStringOrNull("Device");
            terminalRefNumber = parsedJson.getStringOrNull("PosId");

            if (paymentResponse == null) {
                resultId = null;
            } else {
                resultId = paymentResponse.getStringOrNull("ResultId");
            }

            if (paymentDetails != null) {
                if (StringUtils.isNullOrEmpty(transactionId)) {
                    transactionId = parsedJson.getStringOrNull("transactionId ");
                }
                status = paymentDetails.getStringOrNull("transactionStatus ");

                responseCode = paymentDetails.getStringOrNull("resultCode");
                if (StringUtils.isNullOrEmpty(responseCode)) {
                    responseCode = null;
                    TransactionResult mappedTransactionResult = TransactionResult.fromValue(paymentDetails.getStringOrNull("result"));
                    if (mappedTransactionResult != null) {
                        responseCode = mappedTransactionResult.toString();
                    }
                }
                responseText = paymentDetails.getStringOrNull("hostMessage");
                if (StringUtils.isNullOrEmpty(responseText)) {
                    responseText = paymentDetails.getStringOrNull("serverMessage");
                }
                aosa = paymentDetails.getStringOrNull("aosa");
                version = paymentDetails.getStringOrNull("applicationVersion");
                authorizationCode = paymentDetails.getStringOrNull("authorizationCode");
                authorizationMessage = paymentDetails.getStringOrNull("authorizationMessage");

                authorizationMethod = null;
                AuthorizationMethod mappedAuthorizationMethod = AuthorizationMethod.fromValue(paymentDetails.getStringOrNull("authorizationMethod"));
                if (mappedAuthorizationMethod != null) {
                    authorizationMethod = mappedAuthorizationMethod.toString();
                }

                authorizationType = null;
                AuthorizationType mappedAuthorizationType = AuthorizationType.fromValue(paymentDetails.getStringOrNull("authorizationType"));
                if (mappedAuthorizationType != null) {
                    authorizationType = mappedAuthorizationType.toString();
                }

                cardBrandName = paymentDetails.getStringOrNull("cardBrandName") != null ?
                        paymentDetails.getStringOrNull("cardBrandName") :
                        paymentDetails.getStringOrNull("cardBrand");

                cardSource = null;
                CardSource mappedCardSource = CardSource.fromValue(paymentDetails.getStringOrNull("cardSource"));
                if (mappedCardSource != null) {
                    cardSource = mappedCardSource.toString();
                }

                entryMethod = paymentDetails.getStringOrNull("entryMethod");
                cashBackAmount = paymentDetails.getDecimal("cashback"); // TODO(mfranzoy) could it throw a NPE?
                currencyExchangeRate = paymentDetails.getDecimal("currencyExchangeRate"); // TODO(mfranzoy) could it throw a NPE?
                date = paymentDetails.getStringOrNull("date");
                dccCurrencyExponent = paymentDetails.getInt("dccCurrencyExponent"); // TODO(mfranzoy) could it throw a NPE?
                dccText1 = paymentDetails.getStringOrNull("dccText1");
                dccText2 = paymentDetails.getStringOrNull("dccText2");
                errorMessage = paymentDetails.getStringOrNull("errorMessage");

                maskedCardNumber = paymentDetails.getStringOrNull("maskedCardNumber");
                if (maskedCardNumber == null) {
                    maskedCardNumber = paymentDetails.getStringOrNull("maskedCard");
                }

                merchantId = paymentDetails.getStringOrNull("merchantId");

                clientTransactionId = paymentDetails.getStringOrNull("slipNumber");
                terminalCurrency = paymentDetails.getStringOrNull("terminalCurrency");
                terminalId = paymentDetails.getStringOrNull("terminalId");
                terminalPrintingIndicator = paymentDetails.getStringOrNull("terminalPrintingIndicator");
                time = paymentDetails.getStringOrNull("time");
                date = paymentDetails.getStringOrNull("dateTime");
                tipAmount = StringUtils.toAmount(paymentDetails.getStringOrNull("tipAmount"));
                if (tipAmount == null) {
                    tipAmount = StringUtils.toAmount(paymentDetails.getStringOrNull("tip"));
                }
                token = paymentDetails.getStringOrNull("token"); // TODO(mfranzoy) why this is repeated below?
                transactionAmount = StringUtils.toAmount(paymentDetails.getStringOrNull("transactionAmount"));
                if (transactionAmount == null) {
                    transactionAmount = StringUtils.toAmount(paymentDetails.getStringOrNull("requestAmount"));
                }
                transactionAmountInTerminalCurrency = StringUtils.toAmount(paymentDetails.getStringOrNull("transactionAmountInTerminalCurrency"));
                transactionCurrency = paymentDetails.getStringOrNull("transactionCurrency");
                transactionTitle = paymentDetails.getStringOrNull("transactionTitle");

                transactionType = null;
                TransactionType mappedTransactionType = TransactionType.fromValue(paymentDetails.getStringOrNull("type"));
                if (mappedTransactionType != null) {
                    transactionType = mappedTransactionType.toString();
                }

                emvCardTransactionCounter = paymentDetails.getStringOrNull("ATC");
                emvCryptogram = paymentDetails.getStringOrNull("AC");
                emvApplicationId = paymentDetails.getStringOrNull("AID");
                emvTerminalVerificationResults = paymentDetails.getStringOrNull("TVR");
                emvCardHolderVerificationMethod = paymentDetails.getStringOrNull("TSI");
                token = paymentDetails.getStringOrNull("paymentToken");
                if (token == null) {
                    token = paymentDetails.getStringOrNull("token");
                }
                batchNumber = paymentDetails.getStringOrNull("batchNumber");
            }

            command = null;
            if (paymentResponse != null && paymentResponse.has("CloudInfo")) {
                // TODO(mfranzoy) if paymentResponse.get("CloudInfo") is null, we will get a NPE here:
                command = paymentResponse.get("CloudInfo").getStringOrNull("Command");
            } else if (parsedJson.get("CloudInfo") != null) {
                command = parsedJson.get("CloudInfo").getStringOrNull("Command");
            }

            mapBatchValues(parsedJson);

            deviceResponseCode = "00";
        } else {
            deviceResponseCode = "00";
            transactionId = rawResponse;
        }
    }

    protected void mapBatchValues(JsonDoc response) {
        if (response.has("BatchSeqNbr")) {
            sequenceNumber = response.getStringOrNull("BatchSeqNbr");
            totalAmount = response.getStringOrNull("BatchTxnAmt");
            totalCount = response.getStringOrNull("BatchTxnCnt");
        }
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public String getTotalCount() {
        return totalCount;
    }

}

