package com.global.api.gateways;

import com.global.api.builders.*;
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
public class TransitConnector extends XmlGateway implements IPaymentGateway, ISecure3dProvider, IReportingService {
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

    @Override
    public String doTransaction(String request) throws GatewayException {
        String rawResponse = super.doTransaction(request);
        return rawResponse;
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
                .set("currencyCode", builder.getCurrency())
                .set("tokenRequired", builder.isRequestMultiUseToken() ? "Y" : "N")
                .set("externalReferenceID", builder.getClientTransactionId())
                .set("currencyCode", builder.getCurrency())
                .set("orderNumber", builder.getInvoiceNumber());

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
                    .set("cardHolderName", card.getCardHolderName())
                    .set("cardPresentDetail", card.isCardPresent() ? "CARD_PRESENT" : "CARD_NOT_PRESENT")
                    .set("cardholderPresentDetail", cardholderPresentDetail)
                    .set("cardDataInputMode", cardDataInputMode)
                    .set("cardholderAuthenticationMethod", "NOT_AUTHENTICATED")
                    .set("authorizationIndicator", Boolean.TRUE.equals(builder.isAmountEstimated()) ? "PREAUTH" : "FINAL");
        } else if (builder.getPaymentMethod() instanceof ITrackData) {
            ITrackData track = (ITrackData) builder.getPaymentMethod();

            // track data
            String trackData = null;
            TrackNumber trackNumber = track.getTrackNumber();

            // For encrypted data, check if track number is in EncryptionData
            boolean isEncrypted = false;
            if (track instanceof IEncryptable) {
                EncryptionData encryptionData = ((IEncryptable) track).getEncryptionData();
                if (encryptionData != null) {
                    isEncrypted = true;
                    if (!StringUtils.isNullOrEmpty(encryptionData.getTrackNumber())) {
                        // Use track number from encryption data
                        if ("1".equals(encryptionData.getTrackNumber())) {
                            trackNumber = TrackNumber.TrackOne;
                        } else if ("2".equals(encryptionData.getTrackNumber())) {
                            trackNumber = TrackNumber.TrackTwo;
                        }
                    }
                }
            }

            // Use getValue() for encrypted data, getTrackData() for plain text
            if (isEncrypted) {
                trackData = track.getValue();  // Full encrypted blob
            } else {
                trackData = track.getTrackData();  // Parsed/clean track data
            }

            if (!StringUtils.isNullOrEmpty(trackData)) {
                if (TrackNumber.TrackOne.equals(trackNumber)) {
                    request.set("track1Data", trackData);
                } else if (TrackNumber.TrackTwo.equals(trackNumber)) {
                    request.set("track2Data", trackData);
                }
            }

            // Set card presence details
            request.set("cardPresentDetail", "CARD_PRESENT")
                    .set("cardholderPresentDetail", "CARDHOLDER_PRESENT")
                    .set("cardDataInputMode", "MAGNETIC_STRIPE_READER_INPUT")
                    .set("cardholderAuthenticationMethod", "NOT_AUTHENTICATED");

            //figure out if pin present
            if (builder.getPaymentMethod() instanceof IPinProtected) {
                IPinProtected pinProtected = (IPinProtected) builder.getPaymentMethod();
                if (!StringUtils.isNullOrEmpty(pinProtected.getPinBlock())) {
                    request.setHasPin(true);
                }
            }

            // encrypted data
            if (track instanceof IEncryptable) {
                EncryptionData encryptionData = ((IEncryptable) track).getEncryptionData();
                if (encryptionData != null) {
                    // Map encryption type based on version
                    String encryptionType = null;
                    if ("05".equals(encryptionData.getVersion())) {
                        encryptionType = "TDES";
                    } else if ("01".equals(encryptionData.getVersion())) {
                        encryptionType = "VOLTAGE";
                    }

                    if (encryptionType != null) {
                        request.set("encryptionType", encryptionType);
                    }

                    // Add KSN if present
                    if (!StringUtils.isNullOrEmpty(encryptionData.getKsn()) && !request.getHasPin()) {
                        request.set("ksn", encryptionData.getKsn());
                    }
                }
            }

            // emv tags
            if (!StringUtils.isNullOrEmpty(builder.getTagData())) {
                // Parse the tag data and add it to the request
                request.set("emvTags", builder.getTagData());
            }

            // emv fallback
            if (builder.hasEmvFallbackData()) {
                request.set("emvFallbackCondition", EnumUtils.getMapping(Target.Transit, builder.getEmvFallbackCondition()))
                        .set("lastChipRead", EnumUtils.getMapping(Target.Transit, builder.getEmvLastChipRead()))
                        .set("paymentAppVersion", builder.getPaymentApplicationVersion() != null ? builder.getPaymentApplicationVersion() : "unspecified");
            }

            // chip condition fallback
            if (builder.getEmvChipCondition() != null) {
                String emvFallbackCondition = null;
                if (builder.getEmvChipCondition() == EmvChipCondition.ChipFailPreviousSuccess) {
                    emvFallbackCondition = "ICC_TERMINAL_ERROR";
                }
                if (emvFallbackCondition != null) {
                    request.set("emvFallbackCondition", emvFallbackCondition);
                    request.set("paymentAppVersion", "unspecified");
                }
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
                request.set("pin", pinProtected.getPinBlock());
                // Get KSN from EncryptionData
                if (builder.getPaymentMethod() instanceof IEncryptable) {
                    IEncryptable encryptable = (IEncryptable) builder.getPaymentMethod();
                    EncryptionData encryptionData = encryptable.getEncryptionData();
                    if (encryptionData != null && encryptionData.getKsn() != null) {
                        request.set("pinKsn", encryptionData.getKsn());
                    }
                }
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

    @Override
    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        ElementTree et = new ElementTree();
        ReportType reportType = builder.getReportType();

        Element root = et.element("GetReportData");

        et.subElement(root, "deviceID", deviceId);
        et.subElement(root, "transactionKey", transactionKey);

        et.subElement(root, "reportName", mapReportType(reportType));

        if (builder instanceof TransactionReportBuilder) {
            TransactionReportBuilder<T> trb = (TransactionReportBuilder<T>) builder;
            Element searchCriteria = et.subElement(root, "searchCriteria");

            if (trb.getTransactionId() != null) {
                Element condition = et.subElement(searchCriteria, "condition");
                et.subElement(condition, "columnName", "transactionID");
                et.subElement(condition, "operator", "Equals");
                et.subElement(condition, "value", trb.getTransactionId());
            } else if (trb.getStartDate() != null || trb.getEndDate() != null) {
                Element dateRange = et.subElement(searchCriteria, "dateRange");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                if (trb.getStartDate() != null) {
                    et.subElement(dateRange, "fromDate", sdf.format(trb.getStartDate()));
                }
                if (trb.getEndDate() != null) {
                    et.subElement(dateRange, "toDate", sdf.format(trb.getEndDate()));
                }
            }

            if (reportType.equals(ReportType.FindTransactions) && trb.getSearchBuilder() != null) {
                if (trb.getSearchBuilder().getCardNumberLastFour() != null) {
                    Element condition = et.subElement(searchCriteria, "condition");
                    et.subElement(condition, "columnName", "cardNumber");
                    et.subElement(condition, "operator", "Equals");
                    et.subElement(condition, "value", trb.getSearchBuilder().getCardNumberLastFour());
                }

                if (trb.getSearchBuilder().getInvoiceNumber() != null) {
                    Element condition = et.subElement(searchCriteria, "condition");
                    et.subElement(condition, "columnName", "invoiceNumber");
                    et.subElement(condition, "operator", "Equals");
                    et.subElement(condition, "value", trb.getSearchBuilder().getInvoiceNumber());
                }

                if (trb.getSearchBuilder().getClientTransactionId() != null) {
                    Element condition = et.subElement(searchCriteria, "condition");
                    et.subElement(condition, "columnName", "externalReferenceID");
                    et.subElement(condition, "operator", "Equals");
                    et.subElement(condition, "value", trb.getSearchBuilder().getClientTransactionId());
                }
            }

            if (reportType.equals(ReportType.BatchDetail) && trb.getBatchId() != 0) {
                Element condition = et.subElement(searchCriteria, "condition");
                et.subElement(condition, "columnName", "batchNumber");
                et.subElement(condition, "operator", "Equals");
                et.subElement(condition, "value", String.valueOf(trb.getBatchId()));
            }
        }

        if (reportType.equals(ReportType.TransactionDetail) || reportType.equals(ReportType.FindTransactions)) {
            Element optionalColumns = et.subElement(root, "optionalColumns");
            et.subElement(optionalColumns, "columnName", "transactionID");
            et.subElement(optionalColumns, "columnName", "salesTax");
            et.subElement(optionalColumns, "columnName", "transactionType");
            et.subElement(optionalColumns, "columnName", "externalReferenceID");
            et.subElement(optionalColumns, "columnName", "batchNumber");
            et.subElement(optionalColumns, "columnName", "commercialCardIndicator");
            et.subElement(optionalColumns, "columnName", "POnumber");
        }

        if (developerId != null) {
            et.subElement(root, "developerID", developerId);
        }
        String rawResponse = doTransaction(et.toString(root));
        return mapReportResponse(rawResponse, reportType, clazz);
    }

    private String mapReportType(ReportType type) throws UnsupportedTransactionException {
        switch (type) {
            case TransactionDetail:
            case FindTransactions:
            case Activity:
                return "conciseTransactionDetailsReport";
            case BatchDetail:
                return "batchReport";
            case OpenAuths:
                return "authReport";
            default:
                throw new UnsupportedTransactionException("Report type " + type + " is not supported for Transit gateway");
        }
    }

    private <T> T mapReportResponse(String rawResponse, ReportType reportType, Class<T> clazz) throws ApiException {
        Element response = ElementTree.parse(rawResponse).get("GetReportDataResponse");

        String status = response.getString("status");
        String responseCode = response.getString("responseCode");
        String responseMessage = response.getString("responseMessage");

        if (!"PASS".equals(status)) {
            throw new GatewayException(
                    String.format("Report request failed: %s - %s", responseCode, responseMessage),
                    responseCode,
                    responseMessage
            );
        }

        try {
            T rvalue = clazz.newInstance();

            Element reportData = response.get("reportData");
            if (reportData == null) {
                return rvalue;
            }

            if (reportType.equals(ReportType.TransactionDetail)) {
                Element[] rows = reportData.getAll("ROW");
                if (rows != null && rows.length > 0) {
                    TransactionSummary summary = hydrateTransitTransactionSummary(rows[0]);
                    summary.setGatewayResponseCode(responseCode);
                    summary.setGatewayResponseMessage(responseMessage);
                    summary.setStatus(status);
                    rvalue = (T) summary;
                }
            } else if (reportType.equals(ReportType.FindTransactions) ||
                    reportType.equals(ReportType.BatchDetail) ||
                    reportType.equals(ReportType.OpenAuths) ||
                    reportType.equals(ReportType.Activity)) {
                Element[] rows = reportData.getAll("ROW");
                if (rvalue instanceof TransactionSummaryList) {
                    for (Element row : rows) {
                        TransactionSummary summary = hydrateTransitTransactionSummary(row);
                        summary.setGatewayResponseCode(responseCode);
                        summary.setGatewayResponseMessage(responseMessage);
                        summary.setStatus(status);
                        ((TransactionSummaryList) rvalue).add(summary);
                    }
                } else if (rvalue instanceof TransactionSummary && rows != null && rows.length > 0) {
                    TransactionSummary summary = hydrateTransitTransactionSummary(rows[0]);
                    summary.setGatewayResponseCode(responseCode);
                    summary.setGatewayResponseMessage(responseMessage);
                    summary.setStatus(status);
                    rvalue = (T) summary;
                }
            }

            return rvalue;
        } catch (Exception e) {
            throw new ApiException("Error mapping report response: " + e.getMessage(), e);
        }
    }

    private TransactionSummary hydrateTransitTransactionSummary(Element row) {
        TransactionSummary summary = new TransactionSummary();

        String transactionId = row.getString("transactionID");
        if (transactionId == null) {
            transactionId = row.getString("transactionId");
        }
        summary.setTransactionId(transactionId);

        String dateStr = row.getString("transactionDate");
        String timeStr = row.getString("transactionTime");
        if (dateStr != null && timeStr != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date transactionDateTime = sdf.parse(dateStr + " " + timeStr);
                summary.setTransactionDate(new org.joda.time.DateTime(transactionDateTime));
            } catch (Exception e) {
                summary.setTransactionDate(row.getDateTime("transactionDate"));
            }
        } else {
            summary.setTransactionDate(row.getDateTime("transactionDate"));
        }

        summary.setAmount(row.getDecimal("amount"));
        summary.setSettlementAmount(row.getDecimal("amount"));
        summary.setGratuityAmount(row.getDecimal("tipAmount"));
        summary.setTaxAmount(row.getDecimal("salesTax"));

        summary.setAuthCode(row.getString("approvalCode"));
        summary.setInvoiceNumber(row.getString("invoiceNumber"));
        summary.setStatus(row.getString("transactionStatus"));
        summary.setCardType(row.getString("cardType"));
        summary.setMaskedCardNumber(row.getString("cardNumber"));
        summary.setAvsResponseCode(row.getString("avsResponse"));

        // Cardholder info
        summary.setCardHolderName(row.getString("consumerName"));
        summary.setEmail(row.getString("consumerEmailID"));

        // Transaction type mapping
        String transactionType = row.getString("transactionType");
        summary.setTransactionType(transactionType);

        // Reference numbers
        summary.setClientTransactionId(row.getString("externalReferenceID"));

        // Batch info
        summary.setBatchSequenceNumber(row.getString("batchNumber"));

        // Commercial card
        String commercialIndicator = row.getString("commercialCardIndicator");
        // Note: TransactionSummary doesn't have setCommercialIndicator, skip for now
        summary.setPoNumber(row.getString("POnumber"));

        // Additional fields
        Integer deviceIdInt = row.getInt("deviceID");
        if (deviceIdInt != null) {
            summary.setDeviceId(deviceIdInt);
        }

        return summary;
    }

    @Override
    public <T> T surchargeEligibilityLookup(SurchargeEligibilityBuilder builder, Class clazz) throws ApiException {
        return null;
    }
}