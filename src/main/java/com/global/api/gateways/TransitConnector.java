package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.Secure3dBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.network.enums.OperatingEnvironment;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.utils.*;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Accessors(chain = true)
@Setter
public class TransitConnector extends XmlGateway implements IPaymentGateway, ISecure3dProvider {
    private AcceptorConfig acceptorConfig;
    private String deviceId;
    private String developerId;
    private String merchantId;
    private String transactionKey;
    private Secure3dVersion version;

    public TransitConnector() {
    }

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        throw new ApiException("Transit does not support KeepAlive.");
    }

    @Override
    public boolean supportsHostedPayments() {
        return false;
    }

    @Override
    public boolean supportsOpenBanking() {
        return false;
    }

    public String generateKey(String userId, String password) throws ApiException {
        ElementTree et = new ElementTree();

        Element root = et.element("GenerateKey");
        et.subElement(root, "mid").text(merchantId);
        et.subElement(root, "userID").text(userId);
        et.subElement(root, "password").text(password);
        et.subElement(root, "transactionKey", transactionKey);

        String rawResponse = doTransaction(et.toString(root));

        Element response = ElementTree.parse(rawResponse).get("GenerateKeyResponse");
        if ("PASS".equals(response.getString("status"))) {
            transactionKey = response.getString("transactionKey");
            return transactionKey;
        } else {
            String responseCode = response.getString("responseCode");
            String responseMessage = response.getString("responseMessage");
            throw new GatewayException("Failed to generate transaction key for the given credentials", responseCode, responseMessage);
        }
    }

    private String generateManifest(BigDecimal amount, String timestamp) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        TransitRequestBuilder request = new TransitRequestBuilder(mapTransactionType(builder))
                .set("developerID", developerId)
                .set("deviceID", deviceId)
                .set("transactionKey", transactionKey)
                .set("transactionAmount", StringUtils.toNumeric(builder.getAmount()))
                .set("tokenRequired", builder.isRequestMultiUseToken() ? "Y" : "N")
                .set("externalReferenceID", builder.getClientTxnId());

        if (builder.isAllowDuplicates()) {
            request.setAllowDuplicates(true);
        }
        if (builder.isRequestMultiUseToken()) {
            request.set("cardOnFile", builder.isRequestMultiUseToken() ? "Y" : "N");
        }

        String cardDataSource = mapCardDataSource(builder);
        request.set("cardDataSource", cardDataSource);

        if (builder.getPaymentMethod() instanceof ICardData) {
            ICardData card = (ICardData) builder.getPaymentMethod();
            String cardNumber = card.getNumber();
            String cardDataInputMode = "ELECTRONIC_COMMERCE_NO_SECURITY_CHANNEL_ENCRYPTED_SET_WITHOUT_CARDHOLDER_CERTIFICATE";

            if ("Amex".equals(card.getCardType()) && !StringUtils.isNullOrEmpty(card.getCvn())) {
                cardDataInputMode = "MANUALLY_ENTERED_WITH_KEYED_CID_AMEX_JCB";
            } else if (acceptorConfig.getOperatingEnvironment() == OperatingEnvironment.OnPremises_CardAcceptor_Attended) {
                cardDataInputMode = "KEY_ENTERED_INPUT";
            }

            if (card instanceof ITokenizable && ((ITokenizable) card).getToken() != null) {
                cardNumber = ((ITokenizable) card).getToken();
            }

            if (builder.getStoredCredential() != null && builder.getStoredCredential().getInitiator() == StoredCredentialInitiator.Merchant) {
                cardDataInputMode = "MERCHANT_INITIATED_TRANSACTION_CARD_CREDENTIAL_STORED_ON_FILE";
                request.set("cardOnFileTransactionIdentifier", builder.getStoredCredential().getSchemeId());
            }

            String cardholderPresentDetail = card.isCardPresent() ? "CARDHOLDER_PRESENT" : "CARDHOLDER_NOT_PRESENT_ELECTRONIC_COMMERCE";
            if ("MAIL".equals(cardDataSource)) {
                cardholderPresentDetail = "CARDHOLDER_NOT_PRESENT_MAIL_TRANSACTION";
            } else if ("PHONE".equals(cardDataSource)) {
                cardholderPresentDetail = "CARDHOLDER_NOT_PRESENT_PHONE_TRANSACTION";
            }

            request.set("cardNumber", cardNumber)
                    .set("expirationDate", card.getShortExpiry())
                    .set("cvv2", card.getCvn())
                    .set("cardPresentDetail", card.isCardPresent() ? "CARD_PRESENT" : "CARD_NOT_PRESENT")
                    .set("cardholderPresentDetail", cardholderPresentDetail)
                    .set("cardDataInputMode", cardDataInputMode)
                    .set("cardholderAuthenticationMethod", "NOT_AUTHENTICATED")
                    .set("authorizationIndicator", Boolean.TRUE.equals(builder.isAmountEstimated()) ? "PREAUTH" : "FINAL");
        } else if (builder.getPaymentMethod() instanceof ITrackData) {
            ITrackData track = (ITrackData) builder.getPaymentMethod();
            String trackField = TrackNumber.TrackTwo.equals(track.getTrackNumber()) ? "track2Data" : "track1Data";
            request.set(trackField, track.getTrackData());
            request.set("cardPresentDetail", "CARD_PRESENT")
                    .set("cardholderPresentDetail", "CARDHOLDER_PRESENT")
                    .set("cardDataInputMode", "MAGNETIC_STRIPE_READER_INPUT")
                    .set("cardholderAuthenticationMethod", "NOT_AUTHENTICATED");

            if (builder.hasEmvFallbackData()) {
                request.set("emvFallbackCondition", EnumUtils.getMapping(Target.Transit, builder.getEmvFallbackCondition()))
                        .set("lastChipRead", EnumUtils.getMapping(Target.Transit, builder.getEmvLastChipRead()))
                        .set("paymentAppVersion", builder.getPaymentApplicationVersion() != null ? builder.getPaymentApplicationVersion() : "unspecified");
            }
        }

        // AVS
        if (builder.getBillingAddress() != null) {
            request.set("addressLine1", builder.getBillingAddress().getStreetAddress1())
                    .set("zip", builder.getBillingAddress().getPostalCode());
        }

        // PIN Debit
        if (builder.getPaymentMethod() instanceof IPinProtected) {
            IPinProtected pinProtected = (IPinProtected) builder.getPaymentMethod();
            if (!StringUtils.isNullOrEmpty(pinProtected.getPinBlock())) {
                request.set("pin", pinProtected.getPinBlock())
                        .set("pinKsn", pinProtected.getPinBlock().substring(16));
            }
        }

        if (builder.getPaymentMethod() instanceof Credit) {
            Credit pm = (Credit) builder.getPaymentMethod();
            if ("Discover".equals(pm.getCardType()) && "INTERNET".equals(cardDataSource)) {
                request.set("registeredUserIndicator", builder.getLastRegisteredDate() != null ? "YES" : "NO");
                String dateStr = "00/00/0000";
                if (builder.getLastRegisteredDate() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    dateStr = sdf.format(builder.getLastRegisteredDate());
                }
                request.set("lastRegisteredChangeDate", dateStr);
            }
        }

        if (builder.getGratuity() != null) {
            request.set("tip", StringUtils.toNumeric(builder.getGratuity()));
        }

        // 3DS 1/2
        if (builder.getPaymentMethod() instanceof ISecure3d) {
            ISecure3d secure = (ISecure3d) builder.getPaymentMethod();
            if (secure.getThreeDSecure() != null) {
                ThreeDSecure threeDSecure = secure.getThreeDSecure();
                if (Secure3dVersion.ONE.equals(threeDSecure.getVersion())) {
                    request.set("programProtocol", "1");
                } else {
                    request.set("programProtocol", "2")
                            .set("directoryServerTransactionID", threeDSecure.getDirectoryServerTransactionId());
                }

                request.set("eciIndicator", threeDSecure.getEci())
                        .set("secureCode", threeDSecure.getSecureCode())
                        .set("digitalPaymentCryptogram", threeDSecure.getAuthenticationValue())
                        .set("securityProtocol", threeDSecure.getAuthenticationType())
                        .set("ucafCollectionIndicator", EnumUtils.getMapping(Target.Transit, threeDSecure.getUcafIndicator()));
            }
        }

        // Commercial Card Requests
        if (builder.getCommercialData() != null) {
            CommercialData cd = builder.getCommercialData();

            if (TransactionModifier.LevelII.equals(cd.getCommercialIndicator())) {
                request.set("commercialCardLevel", "LEVEL2");
            } else {
                request.set("commercialCardLevel", "LEVEL3");
                request.setProductDetails(cd.getLineItems());
            }

            request.set("salesTax", StringUtils.toNumeric(cd.getTaxAmount()))
                    .set("chargeDescriptor", cd.getDescription())
                    .set("customerRefID", cd.getCustomerReferenceId())
                    .set("purchaseOrder", cd.getPoNumber())
                    .set("shipToZip", cd.getDestinationPostalCode())
                    .set("shipFromZip", cd.getOriginPostalCode())
                    .set("supplierReferenceNumber", cd.getSupplierReferenceNumber())
                    .set("customerVATNumber", cd.getCustomerVAT_Number())
                    .set("summaryCommodityCode", cd.getSummaryCommodityCode())
                    .set("shippingCharges", StringUtils.toNumeric(cd.getFreightAmount()))
                    .set("dutyCharges", StringUtils.toNumeric(cd.getDutyAmount()))
                    .set("destinationCountryCode", cd.getDestinationCountryCode())
                    .set("vatInvoice", cd.getVat_InvoiceNumber());

            if (cd.getOrderDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                request.set("orderDate", sdf.format(cd.getOrderDate()));
            }

            request.setAdditionalTaxDetails(cd.getAdditionalTaxDetails());
        }

        // Acceptor Config
        request.set("terminalCapability", EnumUtils.getMapping(Target.Transit, acceptorConfig.getCardDataInputCapability()))
                .set("terminalCardCaptureCapability", acceptorConfig.isCardCaptureCapability() ? "CARD_CAPTURE_CAPABILITY" : "NO_CAPABILITY")
                .set("terminalOperatingEnvironment", EnumUtils.getMapping(Target.Transit, acceptorConfig.getOperatingEnvironment()))
                .set("cardholderAuthenticationEntity", EnumUtils.getMapping(Target.Transit, acceptorConfig.getCardHolderAuthenticationEntity()))
                .set("cardDataOutputCapability", EnumUtils.getMapping(Target.Transit, acceptorConfig.getCardDataOutputCapability()))
                .set("terminalAuthenticationCapability", EnumUtils.getMapping(Target.Transit, acceptorConfig.getCardHolderAuthenticationCapability()))
                .set("terminalOutputCapability", EnumUtils.getMapping(Target.Transit, acceptorConfig.getTerminalOutputCapability()))
                .set("maxPinLength", EnumUtils.getMapping(Target.Transit, acceptorConfig.getPinCaptureCapability()));

        String response = doTransaction(request.buildRequest(builder));
        return mapResponse(builder, response);
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws ApiException {
        TransitRequestBuilder request = new TransitRequestBuilder(mapTransactionType(builder))
                .set("developerID", developerId)
                .set("deviceID", deviceId)
                .set("transactionKey", transactionKey)
                .set("transactionAmount", StringUtils.toNumeric(builder.getAmount()))
                .set("tip", StringUtils.toNumeric(builder.getGratuity()))
                .set("transactionID", builder.getTransactionId())
                .set("isPartialShipment", builder.isMultiCapture() ? "Y" : null)
                .set("externalReferenceID", builder.getClientTransactionId())
                .set("voidReason", EnumUtils.getMapping(Target.Transit, builder.getVoidReason()));

        request.setPartialShipmentData(builder.getMultiCaptureSequence(), builder.getMultiCapturePaymentCount());

        String response = doTransaction(request.buildRequest(builder));
        return mapResponse(builder, response);
    }

    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public Transaction processSecure3d(Secure3dBuilder builder) throws ApiException {
        throw new UnsupportedOperationException("Not implemented");
    }

    private <T extends TransactionBuilder<Transaction>> Transaction mapResponse(T builder, String rawResponse) throws ApiException {
        String rootName = mapTransactionType(builder) + "Response";

        Element root = ElementTree.parse(rawResponse).get(rootName);
        String status = root.getString("status");
        String responseCode = normalizeResponse(root.getString("responseCode"));
        String responseMessage = root.getString("responseMessage");

        if (!"PASS".equals(status)) {
            throw new GatewayException(
                    String.format("Unexpected Gateway Response: %s - %s", responseCode, responseMessage),
                    responseCode,
                    responseMessage
            );
        }

        Transaction trans = new Transaction();
        trans.setResponseCode(responseCode);
        trans.setResponseMessage(responseMessage);
        trans.setAuthorizationCode(root.getString("authCode"));
        trans.setTransactionId(root.getString("transactionID"));
        trans.setTimestamp(root.getString("transactionTimestamp"));
        trans.setAuthorizedAmount(StringUtils.toAmount(root.getString("processedAmount")));
        trans.setAvsResponseCode(root.getString("addressVerificationCode"));
        trans.setCvnResponseCode(root.getString("cvvVerificationCode"));
        trans.setCardType(root.getString("cardType"));
        trans.setCardLast4(root.getString("maskedCardNumber"));
        trans.setToken(root.getString("token"));
        trans.setCommercialIndicator(root.getString("commercialCard"));
        trans.setBalanceAmount(StringUtils.toAmount(root.getString("balanceAmount")));
        trans.setCardBrandTransactionId(root.getString("cardTransactionIdentifier"));

        // batch response
        if (root.has("batchInfo")) {
            Element batchInfo = root.get("batchInfo");

            BatchSummary batchSummary = new BatchSummary();
            batchSummary.setResponseCode(responseCode);
            batchSummary.setSicCode(batchInfo.getString("SICCODE"));
            batchSummary.setSaleCount(batchInfo.getInt("saleCount"));
            batchSummary.setSaleAmount(StringUtils.toAmount(batchInfo.getString("saleAmount")));
            batchSummary.setReturnCount(batchInfo.getInt("returnCount"));
            batchSummary.setReturnAmount(StringUtils.toAmount(batchInfo.getString("returnAmount")));

            trans.setBatchSummary(batchSummary);
        }

        return trans;
    }

    private <T extends TransactionBuilder<Transaction>> String mapTransactionType(T builder) throws UnsupportedTransactionException {
        switch (builder.getTransactionType()) {
            case Auth:
            case Capture:
                return builder.getTransactionType().toString();
            case Sale:
                if (builder.getPaymentMethod() instanceof Debit) {
                    return "DebitSale";
                }
                return builder.getTransactionType().toString();
            case Balance:
                return "BalanceInquiry";
            case BatchClose:
                return "BatchClose";
            case Edit:
                return "TipAdjustment";
            case Reversal:
            case Void:
                return "Void";
            case Verify:
                return "CardAuthentication";
            case Tokenize:
                return "GetOnusToken";
            case Refund:
                return "Return";
            default:
                throw new UnsupportedTransactionException();
        }
    }

    private String mapCardDataSource(AuthorizationBuilder builder) throws UnsupportedTransactionException {
        IPaymentMethod paymentMethod = builder.getPaymentMethod();
        EcommerceInfo ecommerceInfo = builder.getEcommerceInfo();

        if (paymentMethod instanceof ICardData) {
            ICardData card = (ICardData) paymentMethod;
            if (card.isReaderPresent() && card.isCardPresent()) {
                return "MANUAL";
            }

            if (ecommerceInfo == null) {
                return "INTERNET";
            }

            switch (ecommerceInfo.getChannel()) {
                case Ecom:
                    return "INTERNET";
                case Moto:
                    return "PHONE|MAIL";
                case Mail:
                    return "MAIL";
                case Phone:
                    return "PHONE";
                default:
                    return "INTERNET";
            }
        } else if (paymentMethod instanceof ITrackData) {
            ITrackData track = (ITrackData) paymentMethod;
            if (builder.getTagData() != null) {
                return EntryMethod.Swipe.equals(track.getEntryMethod()) ? "EMV" : "EMV_CONTACTLESS";
            } else if (builder.hasEmvFallbackData()) {
                return "FALLBACK_SWIPE";
            }
            return "SWIPE";
        }

        throw new UnsupportedTransactionException();
    }

    private String normalizeResponse(String input) {
        if ("A0000".equals(input) || "A0014".equals(input) || "A3200".equals(input)) {
            return "00";
        } else if ("A0002".equals(input) || "A3207".equals(input)) {
            return "10";
        }
        return input;
    }

    public String createManifest() throws ApiException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
            String dateFormatString = sdf.format(new Date());

            String plainText = StringUtils.padRight(merchantId, 20, ' ') +
                    StringUtils.padRight(deviceId, 24, ' ') +
                    "000000000000" +
                    StringUtils.padRight(dateFormatString, 8, ' ');

            String tempTransactionKey = transactionKey.substring(0, 16);
            byte[] keyBytes = tempTransactionKey.getBytes(StandardCharsets.UTF_8);

            // AES encryption
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(keyBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] encrypted = cipher.doFinal(plainTextBytes);

            StringBuilder encryptedHex = new StringBuilder();
            for (byte b : encrypted) {
                encryptedHex.append(String.format("%02x", b));
            }
            String encryptedData = encryptedHex.toString();

            String hashKey = hashHmac(transactionKey, transactionKey);

            return hashKey.substring(0, 4) + encryptedData + hashKey.substring(hashKey.length() - 4);
        } catch (Exception e) {
            throw new ApiException("Error creating manifest", e);
        }
    }

    private String hashHmac(String message, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacMD5");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacMD5");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Getters for the properties
    public AcceptorConfig getAcceptorConfig() {
        return acceptorConfig;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeveloperId() {
        return developerId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getTransactionKey() {
        return transactionKey;
    }

    public Secure3dVersion getVersion() {
        return version;
    }
}