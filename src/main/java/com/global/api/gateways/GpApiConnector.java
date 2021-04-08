package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.ReportBuilder;
import com.global.api.builders.TransactionReportBuilder;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.GpApiTokenResponse;
import com.global.api.mapping.GpApiMapping;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.*;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.global.api.entities.enums.TransactionType.Refund;
import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiConnector extends RestGateway implements IPaymentGateway, IReportingService {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";  // Standard expected GP API DateTime format
    public static final String DATE_TIME_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ss.SSS";   // Slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_3 = "yyyy-MM-dd'T'HH:mm:ss";       // Another slightly different GP API DateTime format
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_TIME_DTF = DateTimeFormat.forPattern(DATE_TIME_PATTERN);
    public static final DateTimeFormatter DATE_TIME_DTF_2 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_2);
    public static final DateTimeFormatter DATE_TIME_DTF_3 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_3);
    public static final SimpleDateFormat DATE_SDF = new SimpleDateFormat(DATE_PATTERN);

    private static final String GP_API_VERSION = "2020-04-10";
    private static final String IDEMPOTENCY_HEADER = "x-gp-idempotency";

    private static String accessToken;
    private GpApiConfig gpApiConfig; // Contains: appId, appKey, secondsToExpire, intervalToExpire, channel and language

    private static String dataAccountName;

    public String getDataAccountName() throws GatewayException {
        if (StringUtils.isNullOrEmpty(dataAccountName)) {
            throw new GatewayException("dataAccountName is not set");
        }
        return dataAccountName;
    }

    public void setDataAccountName(String value) {
        dataAccountName = value;
    }

    private static String disputeManagementAccountName;

    public String getDisputeManagementAccountName() throws GatewayException {
        if (StringUtils.isNullOrEmpty(disputeManagementAccountName)) {
            throw new GatewayException("disputeManagementAccountName is not set");
        }
        return disputeManagementAccountName;
    }

    public void setDisputeManagementAccountName(String value) {
        disputeManagementAccountName = value;
    }

    private static String tokenizationAccountName;

    public String getTokenizationAccountName() throws GatewayException {
        if (StringUtils.isNullOrEmpty(tokenizationAccountName)) {
            throw new GatewayException("tokenizationAccountName is not set");
        }
        return tokenizationAccountName;
    }

    public void setTokenizationAccountName(String value) {
        tokenizationAccountName = value;
    }

    private static String transactionProcessingAccountName;

    public String getTransactionProcessingAccountName() throws GatewayException {
        if (StringUtils.isNullOrEmpty(transactionProcessingAccountName)) {
            throw new GatewayException("transactionProcessingAccountName is not set");
        }
        return transactionProcessingAccountName;
    }

    public void setTransactionProcessingAccountName(String value) {
        transactionProcessingAccountName = value;
    }

    public GpApiConnector(GpApiConfig config) {
        super();    // ContentType is: "application/json"

        gpApiConfig = config;
        setServiceUrl(gpApiConfig.getEnvironment().equals(Environment.PRODUCTION) ? ServiceEndpoints.GP_API_PRODUCTION.getValue() : ServiceEndpoints.GP_API_TEST.getValue());
        setEnableLogging(gpApiConfig.isEnableLogging());

        headers.put(org.apache.http.HttpHeaders.ACCEPT, "application/json");
        headers.put(org.apache.http.HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.put("X-GP-Version", GP_API_VERSION);
    }

    private void signIn() throws GatewayException {
        GpApiTokenResponse response = getAccessToken();

        accessToken = response.getToken();
        dataAccountName = response.getDataAccountName();
        disputeManagementAccountName = response.getDisputeManagementAccountName();
        tokenizationAccountName = response.getTokenizationAccountName();
        transactionProcessingAccountName = response.getTransactionProcessingAccountName();
    }

    public void SignOut() throws UnsupportedTransactionException {
        throw new UnsupportedTransactionException();
    }

    public GpApiTokenResponse getAccessToken() throws GatewayException {
        String nonce = DateTime.now().toString(DATE_TIME_DTF);

        JsonDoc requestBody =
                new JsonDoc()
                .set("app_id", gpApiConfig.getAppId())
                .set("nonce", nonce)
                .set("secret", generateSecret(nonce, gpApiConfig.getAppKey()))
                .set("grant_type", "client_credentials");

        if(gpApiConfig.getSecondsToExpire() != 0) {
            requestBody.set("seconds_to_expire", gpApiConfig.getSecondsToExpire());
        }

        if(gpApiConfig.getIntervalToExpire() != null) {
            requestBody.set("interval_to_expire", gpApiConfig.getIntervalToExpire());
        }

        String requestBodyStr = requestBody.toString();

        String rawResponse = null;

        try {
            rawResponse = super.doTransaction("POST", "/accesstoken", requestBodyStr, null);
        } catch (GatewayException ex) {
            generateGpApiException(ex.getResponseCode(), ex.getResponseText());
        }

        return new GpApiTokenResponse(rawResponse);
    }

    // A unique string created using the nonce and app-key.
    // This value is used to further authenticate the request.
    // Created as follows - SHA512(NONCE + APP-KEY).
    public String generateSecret(String nonce, String appKey) throws GatewayException {
        String generatedPassword;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(nonce.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest(appKey.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new GatewayException("Algorithm not available in the environment", e);
        }
        return generatedPassword;
    }

    private String doTransactionWithIdempotencyKey(String verb, String endpoint, String data, HashMap<String, String> queryStringParams, String idempotencyKey) throws GatewayException {
        if (!StringUtils.isNullOrEmpty(idempotencyKey)) {
            headers.put(IDEMPOTENCY_HEADER, idempotencyKey);
        }
        try {
            return super.doTransaction(verb, endpoint, data, queryStringParams);
        } finally {
            headers.remove(IDEMPOTENCY_HEADER);
        }
    }

    public String doTransaction(String verb, String endpoint, String data, HashMap<String, String> queryStringParams, String idempotencyKey) throws GatewayException {
        if (isNullOrEmpty(accessToken)) {
            signIn();
        }
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        try {
            return doTransactionWithIdempotencyKey(verb, endpoint, data, queryStringParams, idempotencyKey);
        } catch (GatewayException ex) {
            if ("NOT_AUTHENTICATED".equals(ex.getResponseCode())) {
                signIn();
                headers.put("Authorization", String.format("Bearer %s", accessToken));

                return doTransactionWithIdempotencyKey(verb, endpoint, data, queryStringParams, idempotencyKey);
            }
            generateGpApiException(ex.getResponseCode(), ex.getResponseText());
            throw ex;
        }
    }

    @Override
    protected String handleResponse(GatewayResponse response) throws GatewayException {
        if (response.getStatusCode() != 200 && response.getStatusCode() != 204) {
            generateGpApiException(String.valueOf(response.getStatusCode()), response.getRawResponse());
        }
        return response.getRawResponse();
    }

    private void generateGpApiException(String responseCode, String responseText) throws GatewayException {
        JsonDoc parsedResponse = JsonDoc.parse(responseText);
        if (parsedResponse.has("error_code")) {     // has the expected JSON GP API error format
            String errorCode = parsedResponse.getString("error_code");
            String detailedErrorCode = parsedResponse.getString("detailed_error_code");
            String detailedErrorDescription = parsedResponse.getString("detailed_error_description");

            throw new GatewayException(
                    String.format("Status Code: %s - %s", responseCode, detailedErrorDescription),
                    errorCode,
                    detailedErrorCode
            );
        }
        // Legacy Exception
        throw new GatewayException(String.format("Status Code: %s - %s", responseCode, responseText));
    }

    private String getEntryMode(AuthorizationBuilder builder) {
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();
        if (builder.getPaymentMethod() instanceof ICardData) {
            ICardData card = (ICardData) builder.getPaymentMethod();
            if (card.isReaderPresent())
                return card.isCardPresent() ? "MANUAL" : "IN_APP";
            else
                return card.isCardPresent() ? "MANUAL" : "ECOM";
        } else if (builderPaymentMethod instanceof ITrackData) {
            ITrackData track = (ITrackData) builder.getPaymentMethod();
            if (builder.getTagData() != null) {
                return (track.getEntryMethod() == EntryMethod.Swipe) ? "CHIP" : "CONTACTLESS_CHIP";
            } else if (builder.hasEmvFallbackData()) {
                return "CONTACTLESS_SWIPE";
            }
            return "SWIPE";
        }
        return "ECOM";
    }

    private String getCaptureMode(AuthorizationBuilder builder) {
        if (builder.isMultiCapture())
            return "MULTIPLE";
        else if (builder.getTransactionType() == TransactionType.Auth)
            return "LATER";

        return "AUTO";
    }

    private String getCvvIndicator(CvnPresenceIndicator cvnPresenceIndicator) {
        switch (cvnPresenceIndicator) {
            case Present:
                return "PRESENT";
            case Illegible:
                return "ILLEGIBLE";
            default:
                return "NOT_PRESENT";
        }
    }

    private String getChipCondition(EmvChipCondition emvChipCondition) {
        if (emvChipCondition == null) return "";
        switch (emvChipCondition) {
            case ChipFailPreviousSuccess:
                return "PREV_SUCCESS";
            case ChipFailPreviousFail:
                return "PREV_FAILED";
            default:
                return "UNKNOWN";
        }
    }


    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        if (isNullOrEmpty(accessToken)) {
            signIn();
        }
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        JsonDoc paymentMethod =
                new JsonDoc()
                        .set("entry_mode", getEntryMode(builder)); // [MOTO, ECOM, IN_APP, CHIP, SWIPE, MANUAL, CONTACTLESS_CHIP, CONTACTLESS_SWIPE]

        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();
        TransactionType builderTransactionType = builder.getTransactionType();
        Address builderBillingAddress = builder.getBillingAddress();

        // CardData
        if (builderPaymentMethod instanceof ICardData) {
            ICardData cardData = (ICardData) builderPaymentMethod;

            JsonDoc card = new JsonDoc();
            card.set("number", cardData.getNumber());
            card.set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth().toString(), 2, '0') : null);
            card.set("expiry_year", cardData.getExpYear() != null ? cardData.getExpYear().toString().substring(2, 4) : null);
            //card.set("track", "");
            card.set("tag", builder.getTagData());
            card.set("cvv", cardData.getCvn());
            card.set("cvv_indicator", getCvvIndicator(cardData.getCvnPresenceIndicator())); // [ILLEGIBLE, NOT_PRESENT, PRESENT]
            card.set("avs_address", builderBillingAddress != null ? builderBillingAddress.getStreetAddress1() : "");
            card.set("avs_postal_code", builderBillingAddress != null ? builderBillingAddress.getPostalCode() : "");
            card.set("funding", builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.Debit ? "DEBIT" : "CREDIT"); // [DEBIT, CREDIT]
            card.set("authcode", builder.getOfflineAuthCode());
            //card.set("brand_reference", "")

            if (builder.getEmvChipCondition() != null) {
                card.set("chip_condition", getChipCondition(builder.getEmvChipCondition())); // [PREV_SUCCESS, PREV_FAILED]
            }

            paymentMethod.set("card", card);

            if (builderTransactionType == TransactionType.Verify) {
                if (builder.isRequestMultiUseToken()) {
                    JsonDoc tokenizationData =
                            new JsonDoc()
                                    .set("account_name", tokenizationAccountName)
                                    .set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? java.util.UUID.randomUUID().toString() : builder.getClientTransactionId())
                                    .set("name", "")
                                    .set("card", card);

                    String tokenizationResponse = doTransaction("POST", "/payment-methods", tokenizationData.toString(), null, builder.getIdempotencyKey());
                    return GpApiMapping.mapResponse(tokenizationResponse);
                } else {
                    if (builderPaymentMethod instanceof ITokenizable && !StringUtils.isNullOrEmpty(((ITokenizable) builderPaymentMethod).getToken())) {
                        String tokenizationResponse = doTransaction("GET", "/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken(), null, null, builder.getIdempotencyKey());

                        return GpApiMapping.mapResponse(tokenizationResponse);
                    } else {
                        JsonDoc verificationData = new JsonDoc()
                                .set("account_name", getTransactionProcessingAccountName())
                                .set("channel", gpApiConfig.getChannel())
                                .set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? java.util.UUID.randomUUID().toString() : builder.getClientTransactionId())
                                .set("currency", builder.getCurrency())
                                .set("country", (builder.getBillingAddress() != null && builder.getBillingAddress().getCountry() != null) ? builder.getBillingAddress().getCountry() : gpApiConfig.getCountry())
                                .set("payment_method", paymentMethod);

                        String verificationResponse = doTransaction("POST", "/verifications", verificationData.toString(), null, builder.getIdempotencyKey());

                        return GpApiMapping.mapResponse(verificationResponse);
                    }
                }
            }
        }

        // TrackData
        else if (builderPaymentMethod instanceof ITrackData) {
            ITrackData track = (ITrackData) builderPaymentMethod;

            JsonDoc card =
                    new JsonDoc()
                            .set("track", track.getValue())
                            .set("tag", builder.getTagData())
                            //.set("cvv", cardData.getCvn())
                            //.set("cvv_indicator", "") // [ILLEGIBLE, NOT_PRESENT, PRESENT]
                            .set("avs_address", builderBillingAddress != null ? builderBillingAddress.getStreetAddress1() : "")
                            .set("avs_postal_code", builderBillingAddress != null ? builderBillingAddress.getPostalCode() : "")
                            .set("authcode", builder.getOfflineAuthCode());
            //.set("brand_reference", "")

            if (builderTransactionType == TransactionType.Sale || builderTransactionType == TransactionType.Refund) {
                if(StringUtils.isNullOrEmpty(track.getValue())) {
                    card.set("number", track.getPan());
                    card.set("expiry_month", track.getExpiry().substring(2, 4));
                    card.set("expiry_year", track.getExpiry().substring(0, 2));
                }
                if (StringUtils.isNullOrEmpty(builder.getTagData())) {
                    card.set("chip_condition", getChipCondition(builder.getEmvChipCondition())); // [PREV_SUCCESS, PREV_FAILED]
                }
            }

            if (builderTransactionType == TransactionType.Sale) {
                card.set("funding", builderPaymentMethod.getPaymentMethodType() == PaymentMethodType.Debit ? "DEBIT" : "CREDIT"); // [DEBIT, CREDIT]
            }

            paymentMethod.set("card", card);
        }

        // Tokenized Payment Method
        if (builderPaymentMethod instanceof ITokenizable) {
            String token = ((ITokenizable) builderPaymentMethod).getToken();
            if (!StringUtils.isNullOrEmpty(token)) {
                paymentMethod.set("id", token);
            }
        }

        // Pin Block
        if (builderPaymentMethod instanceof IPinProtected) {
            paymentMethod.get("card").set("pin_block", ((IPinProtected) builderPaymentMethod).getPinBlock());
        }

        // Authentication
        if (builderPaymentMethod instanceof CreditCardData) {
            CreditCardData creditCardData = (CreditCardData) builderPaymentMethod;
            paymentMethod.set("name", creditCardData.getCardHolderName());

            ThreeDSecure secureEcom = creditCardData.getThreeDSecure();
            if (secureEcom != null) {
                JsonDoc three_ds = new JsonDoc()
                        // Indicates the version of 3DS
                        .set("message_version", secureEcom.getMessageVersion())
                        // An indication of the degree of the authentication and liability shift obtained for this transaction.
                        // It is determined during the 3D Secure process.
                        .set("eci", secureEcom.getEci())
                        // The authentication value created as part of the 3D Secure process.
                        .set("value", secureEcom.getAuthenticationValue())
                        // The reference created by the 3DSecure provider to identify the specific authentication attempt.
                        .set("server_trans_ref", secureEcom.getServerTransactionId())
                        // The reference created by the 3DSecure Directory Server to identify the specific authentication attempt.
                        .set("ds_trans_ref", secureEcom.getDirectoryServerTransactionId());

                JsonDoc authentication = new JsonDoc().set("three_ds", three_ds);

                paymentMethod.set("authentication", authentication);
            }
        }

        // Encryption
        if (builderPaymentMethod instanceof IEncryptable) {
            IEncryptable encryptable = (IEncryptable) builderPaymentMethod;
            EncryptionData encryptionData = encryptable.getEncryptionData();

            if (encryptionData != null) {
                JsonDoc encryption =
                        new JsonDoc()
                                .set("version", encryptionData.getVersion());

                if (!StringUtils.isNullOrEmpty(encryptionData.getKtb())) {
                    encryption.set("method", "KTB");
                    encryption.set("info", encryptionData.getKtb());
                } else if (!StringUtils.isNullOrEmpty(encryptionData.getKsn())) {
                    encryption.set("method", "KSN");
                    encryption.set("info", encryptionData.getKsn());
                }

                if (encryption.has("info")) {
                    paymentMethod.set("encryption", encryption);
                }
            }
        }

        JsonDoc data = new JsonDoc()
                .set("account_name", getTransactionProcessingAccountName())
                .set("type", builderTransactionType == Refund ? "REFUND" : "SALE") // [SALE, REFUND]
                .set("channel", gpApiConfig.getChannel()) // [CP, CNP]
                .set("capture_mode", getCaptureMode(builder)) // [AUTO, LATER, MULTIPLE]
                //.set("remaining_capture_count", "") // Pending Russell
                .set("authorization_mode", builder.isAllowPartialAuth() ? "PARTIAL" : "WHOLE")
                .set("amount", StringUtils.toNumeric(builder.getAmount()))
                .set("currency", builder.getCurrency())
                .set("reference", isNullOrEmpty(builder.getClientTransactionId()) ? java.util.UUID.randomUUID().toString() : builder.getClientTransactionId())
                .set("description", builder.getDescription())
                .set("order_reference", builder.getOrderId())
                .set("gratuity_amount", StringUtils.toNumeric(builder.getGratuity()))
                .set("cashback_amount", StringUtils.toNumeric(builder.getCashBackAmount()))
                .set("surcharge_amount", StringUtils.toNumeric(builder.getSurchargeAmount()))
                .set("convenience_amount", StringUtils.toNumeric(builder.getConvenienceAmount()))
                .set("country", (builderBillingAddress != null && builderBillingAddress.getCountry() != null) ? builderBillingAddress.getCountry() : gpApiConfig.getCountry())
                //.set("language", language)
                .set("ip_address", builder.getCustomerIpAddress())
                //.set("site_reference", "") //
                .set("payment_method", paymentMethod);

        // Stored Credential
        if (builder.getStoredCredential() != null) {
            data.set("initiator", EnumUtils.getMapping(builder.getStoredCredential().getInitiator(), Target.GP_API));
            JsonDoc storedCredential = new JsonDoc()
                    .set("model", EnumUtils.getMapping(builder.getStoredCredential().getType(), Target.GP_API))
                    .set("reason", EnumUtils.getMapping(builder.getStoredCredential().getReason(), Target.GP_API))
                    .set("sequence", EnumUtils.getMapping(builder.getStoredCredential().getSequence(), Target.GP_API));
            data.set("stored_credential", storedCredential);
        }

        String rawResponse = doTransaction("POST", "/transactions", data.toString(), null, builder.getIdempotencyKey());

        return GpApiMapping.mapResponse(rawResponse);
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws GatewayException {
        String response = null;

        JsonDoc data = new JsonDoc();

        TransactionType builderTransactionType = builder.getTransactionType();
        IPaymentMethod builderPaymentMethod = builder.getPaymentMethod();

        if (builderTransactionType == TransactionType.Capture) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            data.set("gratuity_amount", StringUtils.toNumeric(builder.getGratuity()));
            response = doTransaction("POST", "/transactions/" + builder.getTransactionId() + "/capture", data.toString(), null, builder.getIdempotencyKey());
        } else if (builderTransactionType == TransactionType.Refund) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            response = doTransaction("POST", "/transactions/" + builder.getTransactionId() + "/refund", data.toString(), null, builder.getIdempotencyKey());
        } else if (builderTransactionType == TransactionType.Reversal) {
            data.set("amount", StringUtils.toNumeric(builder.getAmount()));
            response = doTransaction("POST", "/transactions/" + builder.getTransactionId() + "/reversal", data.toString(), null, builder.getIdempotencyKey());
        } else if (builderTransactionType == TransactionType.TokenUpdate && builderPaymentMethod instanceof CreditCardData) {
            CreditCardData cardData = (CreditCardData) builderPaymentMethod;

            JsonDoc card =
                    new JsonDoc()
                            .set("expiry_month", cardData.getExpMonth() != null ? StringUtils.padLeft(cardData.getExpMonth().toString(), 2, '0') : "")
                            .set("expiry_year", cardData.getExpYear() != null ? StringUtils.padLeft(cardData.getExpYear().toString(), 4, '0').substring(2, 4) : "");

            JsonDoc payload =
                    new JsonDoc()
                            .set("card", card);

            response = doTransaction("PATCH", "/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken() + "/edit", payload.toString(), null, builder.getIdempotencyKey());
        } else if (builderTransactionType == TransactionType.TokenDelete && builderPaymentMethod instanceof ITokenizable) {
            response = doTransaction("POST", "/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken() + "/delete", "", null, builder.getIdempotencyKey());
        } else if (builderTransactionType == TransactionType.Detokenize && builderPaymentMethod instanceof ITokenizable) {
            response = doTransaction("POST", "/payment-methods/" + ((ITokenizable) builderPaymentMethod).getToken() + "/detokenize", "", null, builder.getIdempotencyKey());
        } else if (builderTransactionType == TransactionType.DisputeAcceptance) {
            response = doTransaction("POST", "/disputes/" + builder.getDisputeId() + "/acceptance", "", null, builder.getIdempotencyKey());
        } else if (builderTransactionType == TransactionType.DisputeChallenge) {
            JsonArray documentsJsonArray = new JsonArray();
            for(DisputeDocument document : builder.getDisputeDocuments()) {
                JsonObject innerJsonDoc = new JsonObject();

                if(document.getType() != null ) {
                    innerJsonDoc.add("type", new JsonPrimitive(document.getType()));
                }

                innerJsonDoc.add("b64_content", new JsonPrimitive(document.getBase64Content()));

                documentsJsonArray.add(innerJsonDoc);
            }

            JsonObject disputeChallengeData = new JsonObject();
            disputeChallengeData.add("documents", documentsJsonArray);

            response = doTransaction("POST", "/disputes/" + builder.getDisputeId() + "/challenge", disputeChallengeData.toString(), null, builder.getIdempotencyKey());
        }
        return GpApiMapping.mapResponse(response);
    }

    @SuppressWarnings("unchecked")
    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        if (isNullOrEmpty(accessToken)) {
            signIn();
        }
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        String reportUrl;
        HashMap<String, String> queryStringParams = new HashMap<String, String>();
        String response;

        if (builder instanceof TransactionReportBuilder) {

            TransactionReportBuilder<TransactionSummary> trb = (TransactionReportBuilder<TransactionSummary>) builder;

            switch (builder.getReportType()) {

                case TransactionDetail:
                    reportUrl = "/transactions/" + trb.getTransactionId();
                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapTransactionSummary(JsonDoc.parse(response));

                case FindTransactions:
                    reportUrl = "/transactions";

                    addQueryStringParam(queryStringParams, "page", String.valueOf(trb.getPage()));
                    addQueryStringParam(queryStringParams, "page_size", String.valueOf(trb.getPageSize()));
                    addQueryStringParam(queryStringParams, "order_by", getValueIfNotNull(trb.getTransactionOrderBy()));
                    addQueryStringParam(queryStringParams, "order", getValueIfNotNull(trb.getTransactionOrder()));
                    addQueryStringParam(queryStringParams, "id", trb.getTransactionId());
                    addQueryStringParam(queryStringParams, "type", getValueIfNotNull(trb.getSearchBuilder().getPaymentType()));
                    addQueryStringParam(queryStringParams, "channel", getValueIfNotNull(trb.getSearchBuilder().getChannel()));
                    addQueryStringParam(queryStringParams, "amount", StringUtils.toNumeric(trb.getSearchBuilder().getAmount()));
                    addQueryStringParam(queryStringParams, "currency", trb.getSearchBuilder().getCurrency());
                    addQueryStringParam(queryStringParams, "number_first6", trb.getSearchBuilder().getCardNumberFirstSix());
                    addQueryStringParam(queryStringParams, "number_last4", trb.getSearchBuilder().getCardNumberLastFour());
                    addQueryStringParam(queryStringParams, "token_first6", trb.getSearchBuilder().getTokenFirstSix());
                    addQueryStringParam(queryStringParams, "token_last4", trb.getSearchBuilder().getTokenLastFour());
                    addQueryStringParam(queryStringParams, "account_name", trb.getSearchBuilder().getAccountName());
                    addQueryStringParam(queryStringParams, "brand", trb.getSearchBuilder().getCardBrand());
                    addQueryStringParam(queryStringParams, "brand_reference", trb.getSearchBuilder().getBrandReference());
                    addQueryStringParam(queryStringParams, "authcode", trb.getSearchBuilder().getAuthCode());
                    addQueryStringParam(queryStringParams, "reference", trb.getSearchBuilder().getReferenceNumber());
                    addQueryStringParam(queryStringParams, "status", getValueIfNotNull(trb.getSearchBuilder().getTransactionStatus()));
                    addQueryStringParam(queryStringParams, "from_time_created", getDateIfNotNull(trb.getStartDate()));
                    addQueryStringParam(queryStringParams, "to_time_created", getDateIfNotNull(trb.getEndDate()));
                    addQueryStringParam(queryStringParams, "country", trb.getSearchBuilder().getCountry());
                    addQueryStringParam(queryStringParams, "batch_id", trb.getSearchBuilder().getBatchId());
                    addQueryStringParam(queryStringParams, "entry_mode", getValueIfNotNull(trb.getSearchBuilder().getPaymentEntryMode()));
                    addQueryStringParam(queryStringParams, "name", trb.getSearchBuilder().getName());

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapTransactions(JsonDoc.parse(response));

                case FindSettlementTransactions:
                    reportUrl = "/settlement/transactions";

                    addQueryStringParam(queryStringParams, "page", String.valueOf(trb.getPage()));
                    addQueryStringParam(queryStringParams, "page_size", String.valueOf(trb.getPageSize()));
                    addQueryStringParam(queryStringParams, "order", getValueIfNotNull(trb.getTransactionOrder()));
                    addQueryStringParam(queryStringParams, "order_by", getValueIfNotNull(trb.getTransactionOrderBy()));
                    addQueryStringParam(queryStringParams, "number_first6", trb.getSearchBuilder().getCardNumberFirstSix());
                    addQueryStringParam(queryStringParams, "number_last4", trb.getSearchBuilder().getCardNumberLastFour());
                    addQueryStringParam(queryStringParams, "deposit_status", getValueIfNotNull(trb.getSearchBuilder().getDepositStatus()));
                    addQueryStringParam(queryStringParams, "account_name", getDataAccountName());
                    addQueryStringParam(queryStringParams, "brand", trb.getSearchBuilder().getCardBrand());
                    addQueryStringParam(queryStringParams, "arn", trb.getSearchBuilder().getAquirerReferenceNumber());
                    addQueryStringParam(queryStringParams, "brand_reference", trb.getSearchBuilder().getBrandReference());
                    addQueryStringParam(queryStringParams, "authcode", trb.getSearchBuilder().getAuthCode());
                    addQueryStringParam(queryStringParams, "reference", trb.getSearchBuilder().getReferenceNumber());
                    addQueryStringParam(queryStringParams, "status", getValueIfNotNull(trb.getSearchBuilder().getTransactionStatus()));
                    addQueryStringParam(queryStringParams, "from_time_created", getDateIfNotNull(trb.getStartDate()));
                    addQueryStringParam(queryStringParams, "to_time_created", getDateIfNotNull(trb.getEndDate()));
                    addQueryStringParam(queryStringParams, "deposit_id", trb.getSearchBuilder().getDepositId());
                    addQueryStringParam(queryStringParams, "from_deposit_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartDepositDate()));
                    addQueryStringParam(queryStringParams, "to_deposit_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndDepositDate()));
                    addQueryStringParam(queryStringParams, "from_batch_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartBatchDate()));
                    addQueryStringParam(queryStringParams, "to_batch_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndBatchDate()));
                    addQueryStringParam(queryStringParams, "system.mid", trb.getSearchBuilder().getMerchantId());
                    addQueryStringParam(queryStringParams, "system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapTransactions(JsonDoc.parse(response));

                case DepositDetail:
                    reportUrl = "/settlement/deposits/" + trb.getSearchBuilder().getDepositId();

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapDepositSummary(JsonDoc.parse(response));

                case FindDeposits:
                    reportUrl = "/settlement/deposits";

                    addQueryStringParam(queryStringParams, "account_name", getDataAccountName());
                    addQueryStringParam(queryStringParams, "page", String.valueOf(trb.getPage()));
                    addQueryStringParam(queryStringParams, "page_size", String.valueOf(trb.getPageSize()));
                    addQueryStringParam(queryStringParams, "order_by", getValueIfNotNull(trb.getDepositOrderBy()));
                    addQueryStringParam(queryStringParams, "order", getValueIfNotNull(trb.getDepositOrder()));
                    addQueryStringParam(queryStringParams, "account_name", getDataAccountName());
                    addQueryStringParam(queryStringParams, "from_time_created", getDateIfNotNull(trb.getStartDate()));
                    addQueryStringParam(queryStringParams, "to_time_created", getDateIfNotNull(trb.getEndDate()));
                    addQueryStringParam(queryStringParams, "id", trb.getSearchBuilder().getDepositId());
                    addQueryStringParam(queryStringParams, "status", getValueIfNotNull(trb.getSearchBuilder().getDepositStatus()));
                    addQueryStringParam(queryStringParams, "amount", StringUtils.toNumeric(trb.getSearchBuilder().getAmount()));
                    addQueryStringParam(queryStringParams, "masked_account_number_last4", trb.getSearchBuilder().getAccountNumberLastFour());
                    addQueryStringParam(queryStringParams, "system.mid", trb.getSearchBuilder().getMerchantId());
                    addQueryStringParam(queryStringParams, "system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapDeposits(JsonDoc.parse(response));

                case DisputeDetail:
                    reportUrl = "/disputes/" + trb.getSearchBuilder().getDisputeId();

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapDisputeSummary(JsonDoc.parse(response));

                case FindDisputes:
                    reportUrl = "/disputes";

                    addQueryStringParam(queryStringParams, "page", String.valueOf(trb.getPage()));
                    addQueryStringParam(queryStringParams, "page_size", String.valueOf(trb.getPageSize()));
                    addQueryStringParam(queryStringParams, "order_by", getValueIfNotNull(trb.getDisputeOrderBy()));
                    addQueryStringParam(queryStringParams, "order", getValueIfNotNull(trb.getDisputeOrder()));
                    addQueryStringParam(queryStringParams, "arn", trb.getSearchBuilder().getAquirerReferenceNumber());
                    addQueryStringParam(queryStringParams, "brand", trb.getSearchBuilder().getCardBrand());
                    addQueryStringParam(queryStringParams, "status", getValueIfNotNull(trb.getSearchBuilder().getDisputeStatus()));
                    addQueryStringParam(queryStringParams, "stage", getValueIfNotNull(trb.getSearchBuilder().getDisputeStage()));
                    addQueryStringParam(queryStringParams, "from_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartStageDate()));
                    addQueryStringParam(queryStringParams, "to_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndStageDate()));
                    addQueryStringParam(queryStringParams, "adjustment_funding", getValueIfNotNull(trb.getSearchBuilder().getAdjustmentFunding()));
                    addQueryStringParam(queryStringParams, "from_adjustment_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartAdjustmentDate()));
                    addQueryStringParam(queryStringParams, "to_adjustment_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndAdjustmentDate()));
                    addQueryStringParam(queryStringParams, "system.mid", trb.getSearchBuilder().getMerchantId());
                    addQueryStringParam(queryStringParams, "system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapDisputes(JsonDoc.parse(response));

                case SettlementDisputeDetail:
                    reportUrl = "/settlement/disputes/" + trb.getSearchBuilder().getSettlementDisputeId();

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapDisputeSummary(JsonDoc.parse(response));

                case FindSettlementDisputes:
                    reportUrl = "/settlement/disputes";

                    addQueryStringParam(queryStringParams, "account_name", getDataAccountName());
                    addQueryStringParam(queryStringParams, "page", String.valueOf(trb.getPage()));
                    addQueryStringParam(queryStringParams, "page_size", String.valueOf(trb.getPageSize()));
                    addQueryStringParam(queryStringParams, "order_by", getValueIfNotNull(trb.getDisputeOrderBy()));
                    addQueryStringParam(queryStringParams, "order", getValueIfNotNull(trb.getDisputeOrder()));
                    addQueryStringParam(queryStringParams, "arn", trb.getSearchBuilder().getAquirerReferenceNumber());
                    addQueryStringParam(queryStringParams, "brand", trb.getSearchBuilder().getCardBrand());
                    addQueryStringParam(queryStringParams, "STATUS", getValueIfNotNull(trb.getSearchBuilder().getDisputeStatus()));
                    addQueryStringParam(queryStringParams, "stage", getValueIfNotNull(trb.getSearchBuilder().getDisputeStage()));
                    addQueryStringParam(queryStringParams, "from_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartStageDate()));
                    addQueryStringParam(queryStringParams, "to_stage_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndStageDate()));
                    addQueryStringParam(queryStringParams, "adjustment_funding", getValueIfNotNull(trb.getSearchBuilder().getAdjustmentFunding()));
                    addQueryStringParam(queryStringParams, "from_adjustment_time_created", getDateIfNotNull(trb.getSearchBuilder().getStartAdjustmentDate()));
                    addQueryStringParam(queryStringParams, "to_adjustment_time_created", getDateIfNotNull(trb.getSearchBuilder().getEndAdjustmentDate()));
                    addQueryStringParam(queryStringParams, "system.mid", trb.getSearchBuilder().getMerchantId());
                    addQueryStringParam(queryStringParams, "system.hierarchy", trb.getSearchBuilder().getSystemHierarchy());

                    response = doTransaction("GET", reportUrl, "", queryStringParams, null);
                    return (T) GpApiMapping.mapDisputes(JsonDoc.parse(response));

                default:
                    throw new UnsupportedTransactionException();
            }
        }

        throw new UnsupportedTransactionException();
    }

    private void addQueryStringParam(HashMap<String, String> queryParams, String name, String value) {
        if (!StringUtils.isNullOrEmpty(name) && !StringUtils.isNullOrEmpty(value)) {
            queryParams.put(name, value);
        }
    }

    String getValueIfNotNull(IStringConstant obj) {
        return (obj != null) ? obj.getValue() : "";
    }

    String getValueIfNotNull(IMappedConstant obj) {
        return (obj != null) ? obj.getValue(Target.GP_API) : "";
    }

    public static String getDateIfNotNull(Date obj) {
        return (obj != null) ? DATE_SDF.format(obj) : "";
    }

    public static String getDateIfNotNull(DateTime obj) {
        return getDateIfNotNull(obj.toDate());
    }

    public static Date parseGpApiDate(String dateValue) throws GatewayException {
        try {
            if (StringUtils.isNullOrEmpty(dateValue)) {
                return null;
            }
            return GpApiConnector.DATE_SDF.parse(dateValue);
        } catch (ParseException ex) {
            throw new GatewayException("Date format is not supported.", ex);
        }
    }

    public static DateTime parseGpApiDateTime(String dateValue) throws GatewayException {
        try {
            if (StringUtils.isNullOrEmpty(dateValue)) {
                return null;
            }
            return GpApiConnector.DATE_TIME_DTF.parseDateTime(dateValue);
        } catch (IllegalArgumentException ex) {
            // Some Date values are returned with these slightly different formats:
            // yyyy-MM-dd'T'HH:mm:ss.SSS or yyyy-MM-dd'T'HH:mm:ss
            // instead of expected: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
            // Because the difference is not significant, they are supported too.
            try {
                return GpApiConnector.DATE_TIME_DTF_2.parseDateTime(dateValue);
            } catch (IllegalArgumentException ex2) {
                try {
                    return GpApiConnector.DATE_TIME_DTF_3.parseDateTime(dateValue);
                } catch (IllegalArgumentException ex3) {
                    throw new GatewayException("DateTime format is not supported.", ex3);
                }
            }
        }
    }

    public String serializeRequest(AuthorizationBuilder builder) {
        throw new NotImplementedException();
    }

    public NetworkMessageHeader sendKeepAlive() {
        throw new NotImplementedException();
    }

    public boolean supportsHostedPayments() {
        throw new NotImplementedException();
    }

}