package com.global.api.gateways;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.builders.ReportBuilder;
import com.global.api.builders.Secure3dBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.*;
import com.global.api.mapping.GpApiMapping;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiConnector extends RestGateway implements IPaymentGateway, IReportingService, ISecure3dProvider {
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";  // Standard expected GP API DateTime format
    public static final String DATE_TIME_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ss.SSS";   // Slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";    // Another slightly different GP API DateTime format. Appears in Paypal.
    public static final String DATE_TIME_PATTERN_4 = "yyyy-MM-dd'T'HH:mm:ss";       // Another slightly different GP API DateTime format
    public static final String DATE_TIME_PATTERN_5 = "yyyy-MM-dd'T'HH:mm";          // Another slightly different GP API DateTime format

    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_TIME_DTF = DateTimeFormat.forPattern(DATE_TIME_PATTERN);
    public static final DateTimeFormatter DATE_TIME_DTF_2 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_2);
    public static final DateTimeFormatter DATE_TIME_DTF_3 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_3);
    public static final DateTimeFormatter DATE_TIME_DTF_4 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_4);
    public static final DateTimeFormatter DATE_TIME_DTF_5 = DateTimeFormat.forPattern(DATE_TIME_PATTERN_5);
    public static final SimpleDateFormat DATE_SDF = new SimpleDateFormat(DATE_PATTERN);

    private static final String GP_API_VERSION = "2021-03-22";
    private static final String IDEMPOTENCY_HEADER = "x-gp-idempotency";

    private String accessToken;
    @Getter GpApiConfig gpApiConfig; // Contains: appId, appKey, secondsToExpire, intervalToExpire, channel and language

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

    static String transactionProcessingAccountName;

    public String getTransactionProcessingAccountName() throws GatewayException {
        if (StringUtils.isNullOrEmpty(transactionProcessingAccountName)) {
            throw new GatewayException("transactionProcessingAccountName is not set");
        }
        return transactionProcessingAccountName;
    }

    public void setTransactionProcessingAccountName(String value) {
        transactionProcessingAccountName = value;
    }

    public String getMerchantUrl() {
        return !StringUtils.isNullOrEmpty(gpApiConfig.getMerchantId()) ? "/merchants/" + gpApiConfig.getMerchantId() : "";
    }

    @Getter @Setter
    public String challengeNotificationUrl;

    @Getter @Setter
    public String methodNotificationUrl;

    public GpApiConnector(GpApiConfig config) {
        super();    // ContentType is: "application/json"

        gpApiConfig = config;

        setProxy(gpApiConfig.getProxy());
        setServiceUrl(gpApiConfig.getEnvironment().equals(Environment.PRODUCTION) ? ServiceEndpoints.GP_API_PRODUCTION.getValue() : ServiceEndpoints.GP_API_TEST.getValue());

        if (gpApiConfig.getAccessTokenInfo() != null) {
            accessToken = gpApiConfig.getAccessTokenInfo().getToken();
            dataAccountName = gpApiConfig.getAccessTokenInfo().getDataAccountName();
            disputeManagementAccountName = gpApiConfig.getAccessTokenInfo().getDisputeManagementAccountName();
            tokenizationAccountName = gpApiConfig.getAccessTokenInfo().getTokenizationAccountName();
            transactionProcessingAccountName = gpApiConfig.getAccessTokenInfo().getTransactionProcessingAccountName();
        }

        setEnableLogging(gpApiConfig.isEnableLogging());

        headers.put(org.apache.http.HttpHeaders.ACCEPT, "application/json");
        headers.put(org.apache.http.HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.put("X-GP-Version", GP_API_VERSION);
        headers.put("x-gp-sdk", "java;version=" + getReleaseVersion());

        dynamicHeaders = config.getDynamicHeaders();
    }

    // Get the SDK release version
    private String getReleaseVersion() {
        String version = "";
        try {
            Document pomXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("pom.xml"));
            Element pomRoot = (Element) pomXml.getElementsByTagName("project").item(0);
            version = pomRoot.getElementsByTagName("version").item(0).getTextContent();
        } catch (Exception ex) {
            if (gpApiConfig.isEnableLogging())
                System.out.println("JAVA SDK version could not be extracted from pom.xml file.");
        }
        return version;
    }

    void signIn() throws GatewayException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            GpApiTokenResponse response = getAccessToken();

            accessToken = response.getToken();

            if (!StringUtils.isNullOrEmpty(response.getDataAccountName()) && dataAccountName != response.getDataAccountName()) {
                dataAccountName = response.getDataAccountName();
            }
            if (!StringUtils.isNullOrEmpty(response.getDisputeManagementAccountName()) && disputeManagementAccountName != response.getDisputeManagementAccountName()) {
                disputeManagementAccountName = response.getDisputeManagementAccountName();
            }
            if (!StringUtils.isNullOrEmpty(response.getTokenizationAccountName()) && tokenizationAccountName != response.getTokenizationAccountName()) {
                tokenizationAccountName = response.getTokenizationAccountName();
            }
            if (!StringUtils.isNullOrEmpty(response.getTransactionProcessingAccountName()) && transactionProcessingAccountName != response.getTransactionProcessingAccountName()) {
                transactionProcessingAccountName = response.getTransactionProcessingAccountName();
            }
        }
    }

    public GpApiRequest SignOut() throws UnsupportedTransactionException {
        return GpApiSessionInfo.signOut();
    }

    public GpApiTokenResponse getAccessToken() throws GatewayException {
        GpApiRequest request = GpApiSessionInfo.signIn(gpApiConfig.getAppId(), gpApiConfig.getAppKey(), gpApiConfig.getSecondsToExpire(), gpApiConfig.getIntervalToExpire(), gpApiConfig.getPermissions());

        String rawResponse = null;

        try {
            rawResponse = super.doTransaction(request.getVerb().getValue(), request.getEndpoint(), request.getRequestBody(), null);
        } catch (GatewayException ex) {
            generateGpApiException(ex.getResponseCode(), ex.getResponseText());
        }

        return new GpApiTokenResponse(rawResponse);
    }

    private String doTransactionWithIdempotencyKey(GpApiRequest.HttpMethod verb, String endpoint, String data, HashMap<String, String> queryStringParams, String idempotencyKey) throws GatewayException {
        if (!StringUtils.isNullOrEmpty(idempotencyKey)) {
            headers.put(IDEMPOTENCY_HEADER, idempotencyKey);
        }
        try {
            return super.doTransaction(verb.getValue(), endpoint, data, queryStringParams);
        } catch (GatewayException ex) {
            throw ex;
        } finally {
            headers.remove(IDEMPOTENCY_HEADER);
        }
    }

    public String doTransaction(GpApiRequest.HttpMethod verb, String endpoint, String data, HashMap<String, String> queryStringParams, String idempotencyKey) throws GatewayException {
        if (isNullOrEmpty(accessToken)) {
            signIn();
        }
        // TODO: Check if we can move this into singIn()
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        try {
            return doTransactionWithIdempotencyKey(verb, endpoint, data, queryStringParams, idempotencyKey);
        } catch (GatewayException ex) {
            if (
                    "NOT_AUTHENTICATED".equals(ex.getResponseCode())    &&
                    !isNullOrEmpty(gpApiConfig.getAppId())              &&
                    !isNullOrEmpty(gpApiConfig.getAppKey())
            ) {
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
        if(!StringUtils.isNullOrEmpty(responseText)) {
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
        }
        // Legacy Exception
        // throw new GatewayException(String.format("Status Code: %s - %s", responseCode, responseText));
    }

    public Transaction processAuthorization(AuthorizationBuilder builder) throws ApiException {
        if (isNullOrEmpty(accessToken)) {
            signIn();
        }
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        GpApiRequest request = GpApiAuthorizationRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());

            if (builder.getPaymentMethod() instanceof AlternativePaymentMethod && builder.getPaymentMethod().getPaymentMethodType() == PaymentMethodType.APM) {
                return GpApiMapping.MapResponseAPM(response);
            }

            return GpApiMapping.mapResponse(response);
        }
        return null;
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws GatewayException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        GpApiRequest request = GpApiManagementRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());

            if (builder.getPaymentMethod() instanceof TransactionReference && builder.getPaymentMethod().getPaymentMethodType() == PaymentMethodType.APM) {
                return GpApiMapping.MapResponseAPM(response);
            }

            return GpApiMapping.mapResponse(response);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> T processReport(ReportBuilder<T> builder, Class<T> clazz) throws ApiException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        GpApiRequest request = GpApiReportRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), null);

            return GpApiMapping.mapReportResponse(response, builder.getReportType());
        }
        return null;
    }

    public Secure3dVersion getVersion() {
        return Secure3dVersion.ANY;
    }

    public Transaction processSecure3d(Secure3dBuilder builder) throws ApiException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        GpApiRequest request = GpApiSecure3DRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());

            return GpApiMapping.map3DSecureData(response);
        }
        return null;
    }

    // --------------------------------------------------------------------------------
    // NOT IMPLEMENTED METHODS FROM IMPLEMENTING INTERFACES
    // --------------------------------------------------------------------------------
    public boolean supportsHostedPayments() {
        throw new NotImplementedException();
    }

    public String serializeRequest(AuthorizationBuilder builder) {
        throw new NotImplementedException();
    }

    public NetworkMessageHeader sendKeepAlive() {
        throw new NotImplementedException();
    }
    // --------------------------------------------------------------------------------


    // --------------------------------------------------------------------------------
    // UTILITY METHODS
    // --------------------------------------------------------------------------------
    public static String getValueIfNotNull(IStringConstant obj) {
        return (obj != null) ? obj.getValue() : "";
    }

    public static String getValueIfNotNull(IMappedConstant obj) {
        return (obj != null) ? obj.getValue(Target.GP_API) : "";
    }

    public static String getDateIfNotNull(Date obj) {
        return (obj != null) ? DATE_SDF.format(obj) : "";
    }

    public static String getDateIfNotNull(DateTime obj) {
        return (obj != null) ? getDateIfNotNull(obj.toDate()) : "";
    }

    public static String getDateTimeIfNotNull(DateTime obj) {
        return (obj != null) ? DATE_TIME_DTF.parseDateTime(obj.toString()).toString() : "";
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
        if (StringUtils.isNullOrEmpty(dateValue)) {
            return null;
        }

        try {
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
                    try {
                        return GpApiConnector.DATE_TIME_DTF_4.parseDateTime(dateValue);
                    } catch (IllegalArgumentException ex4) {
                        try {
                            return GpApiConnector.DATE_TIME_DTF_5.parseDateTime(dateValue);
                        } catch (IllegalArgumentException ex5) {
                            throw new GatewayException("DateTime format is not supported.", ex5);
                        }
                    }
                }
            }
        }
    }
    // --------------------------------------------------------------------------------

}