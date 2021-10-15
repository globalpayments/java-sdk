package com.global.api.gateways;

import com.global.api.builders.*;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.reporting.AltPaymentData;
import com.global.api.entities.reporting.CheckData;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.*;
import com.global.api.utils.Element;
import com.global.api.utils.ElementTree;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.ReverseStringEnumMap;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class GeniusConnector extends Gateway implements IPaymentGateway {
    private String merchantName;
    private String merchantSiteId;
    private String merchantKey;
    private String registerNumber;
    private String terminalId;

    public boolean supportsHostedPayments() { return false; }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
    public void setMerchantSiteId(String merchantSiteId) {
        this.merchantSiteId = merchantSiteId;
    }
    public void setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
    }
    public void setRegisterNumber(String registerNumber) {
        this.registerNumber = registerNumber;
    }
    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public GeniusConnector() {
        super("application/soap+xml");
    }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        IPaymentMethod paymentMethod = builder.getPaymentMethod();

        // build request
        Element transaction = et.element(mapTransactionType(builder))
            .set("xmlns", "http://schemas.merchantwarehouse.com/merchantware/v46/");

        // Credentials
        Element credentials = et.subElement(transaction, "Credentials");
        et.subElement(credentials, "MerchantName").text(merchantName);
        et.subElement(credentials, "MerchantSiteId").text(merchantSiteId);
        et.subElement(credentials, "MerchantKey").text(merchantKey);

        // Payment Data
        Element paymentData = et.subElement(transaction, "PaymentData");
        boolean useToken = false;
        if (paymentMethod instanceof CreditCardData) {
            CreditCardData card = (CreditCardData) paymentMethod;
            if (card.getToken() != null) {
                if (card.getMobileType() != null) {
                    et.subElement(paymentData, "Source").text("WALLET");
                    et.subElement(paymentData, "WalletId", mapWalletId(card.getMobileType()));
                    et.subElement(paymentData, "EncryptedPaymentData", card.getToken());
                }
                else {
                    et.subElement(paymentData, "Source").text("VAULT");
                    et.subElement(paymentData, "VaultToken", card.getToken());
                    useToken = true;
                }
            }
            else {
                et.subElement(paymentData, "Source").text("KEYED");
                et.subElement(paymentData, "CardNumber", card.getNumber());
                et.subElement(paymentData, "ExpirationDate", card.getShortExpiry());
                et.subElement(paymentData, "CardHolder", card.getCardHolderName());
                et.subElement(paymentData, "CardVerificationValue", card.getCvn());
                et.subElement(paymentData, "CardPresence", card.isCardPresent() ? "PRESENT" : "NOTPRESENT");
            }
        }
        else if (paymentMethod instanceof CreditTrackData) {
            et.subElement(paymentData, "Source").text("READER");

            CreditTrackData track = (CreditTrackData) paymentMethod;
            et.subElement(paymentData, "TrackData", track.getValue());
        }

        // AVS
        if (builder.getBillingAddress() != null) {
            Address address = builder.getBillingAddress();
            et.subElement(paymentData, "AvsStreetAddress", address.getStreetAddress1());
            et.subElement(paymentData, "AvsZipCode", address.getPostalCode());
        }

        // Request
        Element request = et.subElement(transaction, "Request");
        et.subElement(request, "Amount", StringUtils.toCurrencyString(builder.getAmount()));
        et.subElement(request, "CashbackAmount", StringUtils.toCurrencyString(builder.getCashBackAmount()));
        et.subElement(request, "SurchargeAmount", StringUtils.toCurrencyString(builder.getSurchargeAmount()));
        et.subElement(request, "AuthorizationCode", builder.getOfflineAuthCode());

        if (builder.getAutoSubstantiation() != null) {
            Element healthcare = et.subElement(request, "HealthCareAmountDetails");

            AutoSubstantiation auto = builder.getAutoSubstantiation();
            // et.subElement(healthcare, "CopayAmount", auto.get.ToCurrencyString());
            et.subElement(healthcare, "ClinicalAmount", StringUtils.toCurrencyString(auto.getClinicSubTotal()));
            et.subElement(healthcare, "DentalAmount", StringUtils.toCurrencyString(auto.getDentalSubTotal()));
            et.subElement(healthcare, "HealthCareTotalAmount", StringUtils.toCurrencyString(auto.getTotalHelthcareAmount()));
            et.subElement(healthcare, "PrescriptionAmount", StringUtils.toCurrencyString(auto.getPrescriptionSubTotal()));
            et.subElement(healthcare, "VisionAmount", StringUtils.toCurrencyString(auto.getVisionSubTotal()));
        }

        et.subElement(request, "InvoiceNumber", builder.getInvoiceNumber());
        et.subElement(request, "RegisterNumber", registerNumber);
        et.subElement(request, "MerchantTransactionId", builder.getClientTransactionId());
        et.subElement(request, "CardAcceptorTerminalId", terminalId);
        et.subElement(request, "EnablePartialAuthorization", builder.isAllowPartialAuth() ? "true" : "false");
        et.subElement(request, "ForceDuplicate", builder.isAllowDuplicates() ? "true" : "false");

        if (useToken && builder.getStoredCredential() != null) {
            et.subElement(request, "StoredCardReason", EnumUtils.getMapping(builder.getStoredCredential().getInitiator(), Target.Genius));
        }

        String response = doTransaction(buildEnvelope(et, transaction));
        return mapResponse(builder, response);
    }

    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        ElementTree et = new ElementTree();
        TransactionType transactionType = builder.getTransactionType();

        // build request
        Element transaction = et.element(mapTransactionType(builder))
            .set("xmlns", "http://schemas.merchantwarehouse.com/merchantware/v46/");

        // Credentials
        Element credentials = et.subElement(transaction, "Credentials");
        et.subElement(credentials, "MerchantName").text(merchantName);
        et.subElement(credentials, "MerchantSiteId").text(merchantSiteId);
        et.subElement(credentials, "MerchantKey").text(merchantKey);

        // Payment Data
        if (transactionType.equals(TransactionType.Refund)) {
            Element paymentData = et.subElement(transaction, "PaymentData");

            et.subElement(paymentData, "Source").text("PreviousTransaction");
            et.subElement(paymentData, "Token", builder.getTransactionId());
        }

        // Request
        Element request = et.subElement(transaction, "Request");
        if (!transactionType.equals(TransactionType.Refund)) {
            et.subElement(request, "Token", builder.getTransactionId());
        }
        et.subElement(request, "Amount", StringUtils.toCurrencyString(builder.getAmount()));
        et.subElement(request, "InvoiceNumber", builder.getInvoiceNumber());
        et.subElement(request, "RegisterNumber", registerNumber);
        et.subElement(request, "MerchantTransactionId", builder.getClientTransactionId());
        et.subElement(request, "CardAcceptorTerminalId", terminalId);

        if (builder.getPaymentMethod() != null && (transactionType.equals(TransactionType.TokenDelete) || transactionType.equals(TransactionType.TokenUpdate))) {
            CreditCardData card = (CreditCardData) builder.getPaymentMethod();

            et.subElement(request, "VaultToken", card.getToken());
            if (transactionType.equals(TransactionType.TokenUpdate)) {
                et.subElement(request, "ExpirationDate", card.getShortExpiry());
            }
        }

        String response = doTransaction(buildEnvelope(et, transaction));
        return mapResponse(builder, response);
    }

    public <TResult> TResult processReport(ReportBuilder<TResult> builder, Class<TResult> clazz) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public String doTransaction(String request) throws GatewayException {
        GatewayResponse response = sendRequest("POST", "", request);
        if(response.getStatusCode() != 200)
            throw new GatewayException("Unexpected http status code [" + response.getStatusCode() + "]");
        return response.getRawResponse();
    }

    public String buildEnvelope(ElementTree et, Element transaction) {
        et.addNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        et.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema");
        et.addNamespace("soap12", "http://www.w3.org/2003/05/soap-envelope");

        Element envelope = et.element("soap12:Envelope");
        Element body = et.subElement(envelope, "soap12:Body");
        body.append(transaction);

        return et.toString(envelope);
    }

    private String mapTransactionType(TransactionBuilder builder) throws ApiException {
        TransactionType transType = builder.getTransactionType();
        TransactionModifier transMod = builder.getTransactionModifier();

        switch (transType) {
            case Auth:
                if (transMod != null && transMod.equals(TransactionModifier.Offline)) {
                    return "ForceCapture";
                }
                return "Authorize";
            case BatchClose:
                return "SettleBatch";
            case Capture:
                return "Capture";
            case Edit:
                return "AdjustTip";
            //AttachSignature
            //FindBoardedCard
            case Refund:
                return "Refund";
            case Sale:
                return "Sale";
            case TokenDelete:
                return "UnboardCard";
            case TokenUpdate:
                return "UpdateBoardedCard";
            case Verify:
                return "BoardCard";
            case Void:
                return "Void";
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private String mapWalletId(MobilePaymentMethodType mobileType) {
        switch (mobileType) {
            case APPLEPAY:
                return "ApplePay";
            case GOOGLEPAY:
                return "GooglePay";
            default:
                return "Unknown";
        }
    }

    private String mapTaxType(TaxType type) {
        switch (type) {
            case NotUsed:
                return "NotProvided";
            case SalesTax:
                return "Provided";
            case TaxExempt:
                return "Exempt";
            default:
                return "UNKNOWN";
        }
    }

    private <TResult> Transaction mapResponse(TransactionBuilder<TResult> builder, String rawResponse) throws ApiException {
        Element root = ElementTree.parse(rawResponse).get(mapTransactionType(builder) + "Response");

        // check response
        String errorCode = root.getString("ErrorCode");
        String errorMessage = root.getString("ErrorMessage");
        if (!StringUtils.isNullOrEmpty(errorMessage)) {
            throw new GatewayException(
                String.format("Unexpected Gateway Response: %s - %s", errorCode, errorMessage),
                errorCode,
                errorMessage
            );
        }

        Transaction response = new Transaction();
        response.setResponseCode("00");
        response.setResponseMessage(root.getString("ApprovalStatus"));
        response.setTransactionId(root.getString("Token"));
        response.setAuthorizationCode(root.getString("AuthorizationCode"));
        response.setHostResponseDate(root.getDate("TransactionDate"));
        response.setAuthorizedAmount(root.getDecimal("Amount"));
        response.setAvailableBalance(root.getDecimal("RemainingCardBalance"));
        //MaskedCardNumber
        //CardHolder
        response.setCardType(root.getString("CardType"));
        //FsaCard
        //ReaderEntryMode
        response.setAvsResponseCode(root.getString("AvsResponse"));
        response.setCvnResponseCode(root.getString("CvResponse"));
        //ExtraData
        //FraudScoring
        //DebitTraceNumber
        //Rfmiq
        //Invoice
        response.setToken(root.getString("VaultToken"));

        if (root.has("BatchStatus")) {
            BatchSummary summary = new BatchSummary();
            summary.setStatus(root.getString("BatchStatus"));
            summary.setTotalAmount(root.getDecimal("BatchAmount"));
            summary.setTransactionCount(root.getInt("TransactionCount"));
            response.setBatchSummary(summary);
        }

        return response;
    }

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        throw new UnsupportedTransactionException();
    }
}
