package com.global.api.gateways;

import com.global.api.builders.*;
import com.global.api.builders.requestbuilder.gpApi.*;
import com.global.api.entities.FileProcessor;
import com.global.api.entities.RiskAssessment;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.entities.gpApi.GpApiTokenResponse;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.mapping.GpApiMapping;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.paymentMethods.AlternativePaymentMethod;
import com.global.api.paymentMethods.Installment;
import com.global.api.paymentMethods.TransactionReference;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static com.global.api.utils.StringUtils.isNullOrEmpty;

public class GpApiConnector extends RestGateway implements IPaymentGateway, IReportingService, ISecure3dProvider, 
        IPayFacProvider, IFraudCheckService, IFileProcessingService, IRecurringGateway, IDeviceCloudService, IInstallmentService{

    public static final SimpleDateFormat DATE_SDF = DateParsingUtils.DATE_SDF;
    public static final DateTimeFormatter DATE_TIME_DTF = DateParsingUtils.DATE_TIME_DTF;
    private static final String GP_API_VERSION = "2021-03-22";
    private static final String IDEMPOTENCY_HEADER = "x-gp-idempotency";
    public final boolean hasBuiltInMerchantManagementService() {
        return true;
    }
    public boolean supportsRetrieval() throws ApiException { return true; }
    public boolean supportsUpdatePaymentDetails() throws ApiException { throw new ApiException("NOT IMPLEMENTED"); }


    @Override
    public <T> T processRecurring(RecurringBuilder<T> builder, Class<T> clazz) throws ApiException {
        T result = null;
        if (isNullOrEmpty(accessToken)) {
            signIn();
        }
        GpApiRequest request = new GpApiRecurringRequestBuilder().buildRequest(builder, this);
        if (request != null){
            String response = doTransaction(request.getVerb().getValue(), request.getEndpoint(), request.getRequestBody());
            return  GpApiMapping.mapRecurringEntity(response, (T) builder.getEntity(), clazz);

        }
        return result;
    }

    private String accessToken;
    @Getter GpApiConfig gpApiConfig; // Contains: appId, appKey, secondsToExpire, intervalToExpire, channel and language

    public String getMerchantUrl() {
        return !StringUtils.isNullOrEmpty(gpApiConfig.getMerchantId()) ? GpApiRequest.MERCHANT_MANAGEMENT_ENDPOINT + "/" + gpApiConfig.getMerchantId() : "";
    }

    @Getter @Setter
    public String challengeNotificationUrl;

    @Getter @Setter
    public String methodNotificationUrl;

    public GpApiConnector(GpApiConfig config) {
        super();    // ContentType is: "application/json"

        gpApiConfig = config;

        setWebProxy(gpApiConfig.getWebProxy());
        setServiceUrl(gpApiConfig.getEnvironment().equals(Environment.PRODUCTION) ? ServiceEndpoints.GP_API_PRODUCTION.getValue() : ServiceEndpoints.GP_API_TEST.getValue());

        setEnableLogging(gpApiConfig.isEnableLogging());
        setRequestLogger(gpApiConfig.getRequestLogger());

        headers.put(org.apache.http.HttpHeaders.ACCEPT, "application/json");
        headers.put(org.apache.http.HttpHeaders.ACCEPT_ENCODING, "gzip");
        headers.put("X-GP-Version", GP_API_VERSION);
        if (!gpApiConfig.isAndroid()) {
            headers.put("x-gp-sdk", "java;version=" + getReleaseVersion());
        }

        dynamicHeaders = gpApiConfig.getDynamicHeaders();
    }

    // Get the SDK release version
    private String getReleaseVersion() {
        String version = "";
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setExpandEntityReferences(false);
            Document pomXml = factory.newDocumentBuilder().parse(new File("pom.xml"));
            Element pomRoot = (Element) pomXml.getElementsByTagName("project").item(0);
            version = pomRoot.getElementsByTagName("version").item(0).getTextContent();
        } catch (Exception ex) {
            if (gpApiConfig.isEnableLogging())
                System.out.println("JAVA SDK version could not be extracted from pom.xml file.");
        }
        return version;
    }

    public FileProcessor processFileUpload(FileProcessingBuilder builder) throws ApiException {
        if (isNullOrEmpty(accessToken)) {
            signIn();
        }
        GpApiFileProcessingRequestBuilder gpApiFileProcessingRequestBuilder = new GpApiFileProcessingRequestBuilder();
        GpApiRequest request = gpApiFileProcessingRequestBuilder.buildRequest(builder, this);
        if (request != null) {
            String response = doTransaction(request.getVerb().getValue(), request.getEndpoint(),
                    request.getRequestBody(), request.getQueryStringParams());
            return GpApiMapping.MapFileProcessingResponse(response);
        }
        return null;
    }

    void signIn() throws GatewayException {
        AccessTokenInfo accessTokenInfo = gpApiConfig.getAccessTokenInfo();

        if (accessTokenInfo != null && !isNullOrEmpty(accessTokenInfo.getAccessToken())) {
            accessToken = accessTokenInfo.getAccessToken();
            headers.put("Authorization", String.format("Bearer %s", accessToken));
            return;
        }

        headers.remove("Authorization");
        GpApiTokenResponse response = getAccessToken();

        accessToken = response.getToken();
        headers.put("Authorization", String.format("Bearer %s", accessToken));

        if (accessTokenInfo == null) {
            accessTokenInfo = new AccessTokenInfo();
        }

        accessTokenInfo.setMerchantId(response.getMerchantId());

        if (isNullOrEmpty(accessTokenInfo.getAccessToken())) {
            accessTokenInfo.setAccessToken(response.getToken());
        }

        if (isNullOrEmpty(accessTokenInfo.getDataAccountName()) && isNullOrEmpty(accessTokenInfo.getDataAccountID())) {
            accessTokenInfo.setDataAccountID(response.getDataAccountID());
        }

        if (isNullOrEmpty(accessTokenInfo.getTokenizationAccountName()) &&
                isNullOrEmpty(accessTokenInfo.getTokenizationAccountID())) {
            accessTokenInfo.setTokenizationAccountID(response.getTokenizationAccountID());
        }

        if (isNullOrEmpty(accessTokenInfo.getDisputeManagementAccountName()) &&
                isNullOrEmpty(accessTokenInfo.getDisputeManagementAccountID())) {
            accessTokenInfo.setDisputeManagementAccountID(response.getDisputeManagementAccountID());
        }

        if (isNullOrEmpty(accessTokenInfo.getTransactionProcessingAccountName()) &&
                isNullOrEmpty(accessTokenInfo.getTransactionProcessingAccountID())) {
            accessTokenInfo.setTransactionProcessingAccountID(response.getTransactionProcessingAccountID());
        }
        if (isNullOrEmpty(accessTokenInfo.getRiskAssessmentAccountName()) &&
                isNullOrEmpty(accessTokenInfo.getRiskAssessmentAccountID())) {
            accessTokenInfo.setRiskAssessmentAccountID(response.getRiskAssessmentAccountID());
        }
        if (isNullOrEmpty(accessTokenInfo.getMerchantManagementAccountName()) &&
                isNullOrEmpty(accessTokenInfo.getMerchantManagementAccountID())) {
            accessTokenInfo.setMerchantManagementAccountID(response.getMerchantManagementAccountID());
        }

        if (isNullOrEmpty(accessTokenInfo.getFileProcessingAccountName()) &&
                isNullOrEmpty(accessTokenInfo.getFileProcessingAccountID())) {

            accessTokenInfo.setFileProcessingAccountID(response.getFileProcessingAccountID());
        }

        gpApiConfig.setAccessTokenInfo(accessTokenInfo);
    }

    public GpApiRequest signOut() throws GatewayException {
        return gpApiConfig.getAccessTokenProvider().signOut();
    }

    public GpApiTokenResponse getAccessToken() throws GatewayException {
        GpApiRequest request = gpApiConfig.getAccessTokenProvider().signIn(gpApiConfig.getAppId(), gpApiConfig.getAppKey(), gpApiConfig.getSecondsToExpire(), gpApiConfig.getIntervalToExpire(), gpApiConfig.getPermissions(),gpApiConfig.getPorticoTokenConfig());

        String rawResponse = null;

        try {
            rawResponse = super.doTransaction(request.getVerb().getValue(), request.getEndpoint(), request.getRequestBody(), null);
        } catch (GatewayException ex) {
            try {
                Integer.parseInt(ex.getResponseCode());
                generateGpApiException(ex.getResponseCode(), ex.getResponseText());
                throw ex;
            } catch (NumberFormatException nfe) {
                throw ex;
            }
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

        try {
            return doTransactionWithIdempotencyKey(verb, endpoint, data, queryStringParams, idempotencyKey);
        } catch (GatewayException ex) {
            if (
                    ("NOT_AUTHENTICATED".equals(ex.getResponseCode()) ||
                            "401".equals(ex.getResponseCode())) &&
                            !isNullOrEmpty(gpApiConfig.getAppId()) &&
                            !isNullOrEmpty(gpApiConfig.getAppKey())
            ) {
                if (this.gpApiConfig != null && this.gpApiConfig.getAccessTokenInfo() != null) {
                    this.gpApiConfig.getAccessTokenInfo().setAccessToken(null);
                }
                signIn();

                return doTransactionWithIdempotencyKey(verb, endpoint, data, queryStringParams, idempotencyKey);
            }
            try {
                Integer.parseInt(ex.getResponseCode());
                generateGpApiException(ex.getResponseCode(), ex.getResponseText());
                throw ex;
            } catch (NumberFormatException nfe) {
                throw ex;
            }
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

        GpApiAuthorizationRequestBuilder gpApiAuthorizationRequestBuilder = new GpApiAuthorizationRequestBuilder();
        GpApiRequest request = gpApiAuthorizationRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            addMaskedData(request.maskedData);
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());

            if (builder.getPaymentMethod() instanceof AlternativePaymentMethod) {
                return GpApiMapping.mapResponseAPM(response);
            }

            return GpApiMapping.mapResponse(response);
        }
        return null;
    }

    public Transaction manageTransaction(ManagementBuilder builder) throws GatewayException, BuilderException, UnsupportedTransactionException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }

        GpApiManagementRequestBuilder gpApiManagementRequestBuilder = new GpApiManagementRequestBuilder();
        GpApiRequest request = gpApiManagementRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            addMaskedData(request.maskedData);
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());

            if (builder.getPaymentMethod() instanceof TransactionReference && builder.getPaymentMethod().getPaymentMethodType() == PaymentMethodType.APM) {
                return GpApiMapping.mapResponseAPM(response);
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

        GpApiReportRequestBuilder gpApiReportRequestBuilder = new GpApiReportRequestBuilder();
        GpApiRequest request = gpApiReportRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            addMaskedData(request.maskedData);
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), null);
            return GpApiMapping.mapReportResponse(response, builder.getReportType());
        }
        return null;
    }

    @Override
    public <T> T surchargeEligibilityLookup(SurchargeEligibilityBuilder builder, Class clazz) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public Secure3dVersion getVersion() {
        return Secure3dVersion.ANY;
    }

    public Transaction processSecure3d(Secure3dBuilder builder) throws ApiException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }

        GpApiSecureRequestBuilder gpApiSecureRequestBuilder = new GpApiSecureRequestBuilder();
        GpApiRequest request = gpApiSecureRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            addMaskedData(request.maskedData);
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());

            return GpApiMapping.map3DSecureData(response);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T processBoardingUser(PayFacBuilder<T> builder) throws ApiException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }

        GpApiPayFacRequestBuilder gpApiPayFacRequestBuilder = new GpApiPayFacRequestBuilder();
        GpApiRequest request = gpApiPayFacRequestBuilder.buildRequest(builder, this);

        if (request != null){
            addMaskedData(request.maskedData);
            var response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());

            return GpApiMapping.mapMerchantEndpointResponse(response);
        }

        return null;
    }

    @Override
    public <T> T processPayFac(PayFacBuilder<T> builder) throws ApiException {
        throw new UnsupportedTransactionException("Method processPayFac() not supported");
    }

    @Override
    public String processPassThrough(JsonDoc rawRequest) throws ApiException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }

        GpApiRequest request = new GpApiMiCRequestBuilder().buildRequest(rawRequest.toString(), this);

        if (request != null) {
            return doTransaction(
                    request.getVerb().getValue(),
                    request.getEndpoint(),
                    request.getRequestBody(),
                    request.getQueryStringParams());
        }
        return null;
    }

    /**
     * send request to server and return GP API create installment response
     * @param builder
     * @return {@link Installment}
     * @throws ApiException
     */
    public Installment processInstallment(InstallmentBuilder builder) throws ApiException {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            signIn();
        }

        GpApiInstallmentRequestBuilder gpApiInstallmentRequestBuilder = new GpApiInstallmentRequestBuilder();
        GpApiRequest request = gpApiInstallmentRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            addMaskedData(request.maskedData);
            String response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), null);
            return GpApiMapping.mapInstallmentResponse(response);
        }
        return null;
    }

    // --------------------------------------------------------------------------------
    // NOT IMPLEMENTED METHODS FROM IMPLEMENTING INTERFACES
    // --------------------------------------------------------------------------------
    public boolean supportsHostedPayments() {
        return false;
    }

    @Override
    public boolean supportsOpenBanking() {
        return true;
    }

    public String serializeRequest(AuthorizationBuilder builder) throws ApiException {
        throw new UnsupportedTransactionException();
    }

    public NetworkMessageHeader sendKeepAlive() throws ApiException {
        throw new UnsupportedTransactionException();
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
        return DateParsingUtils.parseDateTime(dateValue);
    }

    @Override
    public <T> RiskAssessment processFraud(FraudBuilder<T> builder) throws ApiException {

        if (isNullOrEmpty(accessToken)) {
            signIn();
        }

        GpApiSecureRequestBuilder gpApiSecureRequestBuilder = new GpApiSecureRequestBuilder();
        GpApiRequest request = gpApiSecureRequestBuilder.buildRequest(builder, this);

        if (request != null) {
            addMaskedData(request.maskedData);
            var response = doTransaction(request.getVerb(), request.getEndpoint(), request.getRequestBody(), request.getQueryStringParams(), builder.getIdempotencyKey());
            return GpApiMapping.mapRiskAssessmentResponse(response);
        }

        return null;
    }

    // --------------------------------------------------------------------------------

}