package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.builders.Secure3dBuilder;
import com.global.api.builders.requestbuilder.gpApi.GpApiReportRequestBuilder;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.AuthenticationSortProperty;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.PaymentMethodName;
import com.global.api.entities.enums.MessageCategory;
import com.global.api.entities.enums.MessageVersion;
import com.global.api.entities.enums.ReportType;
import com.global.api.entities.enums.SortDirection;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.exceptions.UnsupportedTransactionException;
import com.global.api.entities.reporting.*;
import com.global.api.entities.gpApi.GpApiRequest;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.ActionSummary;
import com.global.api.entities.reporting.ActionSummaryPaged;
import com.global.api.entities.reporting.AuthenticationSummaryPaged;
import com.global.api.entities.reporting.DepositSummary;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.StoredPaymentMethodSummary;
import com.global.api.entities.enums.ReportType;
import com.global.api.gateways.GpApiConnector;
import com.global.api.mapping.GpApiMapping;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.services.Secure3dService;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.var;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static com.global.api.gateways.GpApiConnector.parseGpApiDate;
import static com.global.api.gateways.GpApiConnector.parseGpApiDateTime;

public class GpApiMappingTest extends BaseGpApiTest {

    public GpApiMappingTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        ServicesContainer.configureService(config);
    }

    @Test
    public void MapTransactionSummaryTest() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"TRN_TvY1QFXxQKtaFSjNaLnDVdo3PZ7ivz\",\"time_created\":\"2020-06-05T03:08:20.896Z\",\"time_last_updated\":\"\",\"status\":\"PREAUTHORIZED\",\"type\":\"SALE\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"TRA_6716058969854a48b33347043ff8225f\",\"account_name\":\"Transaction_Processing\",\"channel\":\"CNP\",\"amount\":\"10000\",\"currency\":\"CAD\",\"reference\":\"My-TRANS-184398775\",\"description\":\"41e7877b-da90-4c5f-befe-7f024b96311e\",\"order_reference\":\"\",\"time_created_reference\":\"\",\"batch_id\":\"\",\"initiator\":\"\",\"country\":\"\",\"language\":\"\",\"ip_address\":\"97.107.232.5\",\"site_reference\":\"\",\"payment_method\":{\"result\":\"00\",\"message\":\"SUCCESS\",\"entry_mode\":\"ECOM\",\"name\":\"NAME NOT PROVIDED\",\"card\":{\"funding\":\"CREDIT\",\"brand\":\"VISA\",\"authcode\":\"12345\",\"brand_reference\":\"TQ76bJf7qzkC30U0\",\"masked_number_first6last4\":\"411111XXXXXX1111\",\"cvv_indicator\":\"PRESENT\",\"cvv_result\":\"MATCHED\",\"avs_address_result\":\"MATCHED\",\"avs_postal_code_result\":\"MATCHED\"}},\"action_create_id\":\"ACT_TvY1QFXxQKtaFSjNaLnDVdo3PZ7ivz\",\"parent_resource_id\":\"TRN_TvY1QFXxQKtaFSjNaLnDVdo3PZ7ivz\",\"action\":{\"id\":\"ACT_kLkU0qND7wyuW0Br76ZNyAnlPTjHsb\",\"type\":\"TRANSACTION_SINGLE\",\"time_created\":\"2020-11-24T15:43:43.990Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"JF2GQpeCrOivkBGsTRiqkpkdKp67Gxi0\",\"app_name\":\"test_app\"}}";

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Act
        TransactionSummary transaction = GpApiMapping.mapTransactionSummary(doc);

        // Assert
        assertEquals(doc.getString("id"), transaction.getTransactionId());
        assertEquals(parseGpApiDateTime(doc.getString("time_created")), transaction.getTransactionDate());
        assertEquals(doc.getString("status"), transaction.getTransactionStatus());
        assertEquals(doc.getString("type"), transaction.getTransactionType());
        assertEquals(doc.getString("channel"), transaction.getChannel());
        assertEquals(doc.getAmount("amount"), transaction.getAmount());
        assertEquals(doc.getString("currency"), transaction.getCurrency());
        assertEquals(doc.getString("reference"), transaction.getReferenceNumber());
        assertEquals(doc.getString("reference"), transaction.getClientTransactionId());
        assertEquals(doc.getString("batch_id"), transaction.getBatchSequenceNumber());
        assertEquals(doc.getString("country"), transaction.getCountry());
        assertEquals(doc.getString("parent_resource_id"), transaction.getOriginalTransactionId());

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            assertEquals(paymentMethod.getString("message"), transaction.getGatewayResponseMessage());
            assertEquals(paymentMethod.getString("entry_mode"), transaction.getEntryMode());
            assertEquals(paymentMethod.getString("name"), transaction.getCardHolderName());

            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");
                assertEquals(card.getString("brand"), transaction.getCardType());
                assertEquals(card.getString("authcode"), transaction.getAuthCode());
                assertEquals(card.getString("brand_reference"), transaction.getBrandReference());
                assertEquals(card.getString("arn"), transaction.getAcquirerReferenceNumber());
                assertEquals(card.getString("masked_number_first6last4"), transaction.getMaskedCardNumber());
            }
        }
    }

    @Test
    public void MapCheck3DSAvailabilityRequestAndResponseTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("Transaction_Processing");
        config.setAccessTokenInfo(accessTokenInfo);

        GpApiConnector gateway = new GpApiConnector(config);
        gateway.setChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/");
        gateway.setMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");

        CreditCardData card = new CreditCardData();
        card.setNumber("4012001038488884");
        card.setExpMonth(12);
        card.setExpYear(2030);

        Secure3dBuilder builder = Secure3dService.checkEnrollment(card)
                .withAccountName("Transaction_Processing")
                .withReferenceNumber("3ds2_ref1")
                .withChannel("CNP")
                .withCountry("FR")
                .withAmount(new BigDecimal("50.89"))
                .withCurrency("EUR")
                .withChallengeNotificationUrl("https://ensi808o85za.x.pipedream.net/")
                .withMethodNotificationUrl("https://ensi808o85za.x.pipedream.net/");

        GpApiRequest request = new com.global.api.builders.requestbuilder.gpApi.GpApiSecureRequestBuilder().buildRequest(builder, gateway);
        JsonDoc requestBody = JsonDoc.parse(request.getRequestBody());

        assertEquals("Transaction_Processing", requestBody.getString("account_name"));
        assertEquals("3ds2_ref1", requestBody.getString("reference"));
        assertEquals("CNP", requestBody.getString("channel"));
        assertEquals("5089", requestBody.getString("amount"));
        assertEquals("EUR", requestBody.getString("currency"));
        assertEquals("FR", requestBody.getString("country"));
        assertEquals("4012001038488884", requestBody.get("payment_method").get("card").getString("number"));
        assertEquals("https://ensi808o85za.x.pipedream.net/", requestBody.get("notifications").getString("challenge_return_url"));
        assertEquals("https://ensi808o85za.x.pipedream.net/", requestBody.get("notifications").getString("three_ds_method_return_url"));

        JsonDoc responseJson = new JsonDoc();
        responseJson.set("id", "AUT_uzFr7t4VOqxdLDI44hHmXIjHtOOE8d");
        responseJson.set("time_created", "2021-05-03T21:23:39.718Z");
        responseJson.set("transaction_type", "SALE");
        responseJson.set("status", "AVAILABLE");
        responseJson.set("channel", "CP");
        responseJson.set("amount", "11099");
        responseJson.set("currency", "USD");
        responseJson.set("country", "AD");
        responseJson.set("merchant_id", "MER_A6A1EC44522F96630ABEA17A");
        responseJson.set("merchant_name", "ABC INDUSTRIES");
        responseJson.set("account_id", "TRA_86920f927028745yt34d077d88beb29b");
        responseJson.set("account_name", "transaction_processing");
        responseJson.set("reference", "becf9f3e-4d33-459c-8ed2-0c4affc9555e");

        JsonDoc threeDs = new JsonDoc();
        threeDs.set("server_trans_ref", "string");
        threeDs.set("message_version", "string");
        threeDs.set("ds_protocol_version_start", "string");
        threeDs.set("ds_protocol_version_end", "string");
        threeDs.set("acs_protocol_version_start", "string");
        threeDs.set("acs_protocol_version_end", "string");
        threeDs.set("acs_info_indicator", new String[]{"NOT_AVAILABLE"});
        threeDs.set("enrolled_status", "ENROLLED");
        threeDs.set("eci", "string");
        threeDs.set("liability_shift", "YES");
        threeDs.set("challenge_model", "STATIC_CHALLENGE");
        threeDs.set("session_data_field_name", "threeDSSessionData");
        threeDs.set("message_type", "PAREQ");
        threeDs.set("challenge_value", "string");
        threeDs.set("redirect_url", "string");
        threeDs.set("acs_challenge_request_url", "string");
        threeDs.set("method_url", "string");

        JsonDoc methodData = new JsonDoc();
        methodData.set("three_ds_server_trans_id", "string");
        methodData.set("three_ds_method_return_url", "string");
        methodData.set("encoded_method_data", "string");
        threeDs.set("method_data", methodData);
        responseJson.set("three_ds", threeDs);

        JsonDoc notifications = new JsonDoc();
        notifications.set("challenge_return_url", "string");
        responseJson.set("notifications", notifications);

        JsonDoc action = new JsonDoc();
        action.set("id", "ACT_uzFr7t4VOqxdLDI44hHmXIjHtOOE8d");
        action.set("type", "INITIATE");
        action.set("time_created", "2021-05-03T21:23:39.718Z");
        action.set("result_code", "SUCCESS");
        action.set("app_id", "uzFr7t4VOqxdLDI44hHmXIjHtOOE8d");
        action.set("app_name", "my_lovely_app");
        responseJson.set("action", action);

        Transaction mapped = GpApiMapping.map3DSecureData(responseJson.toString());
        ThreeDSecure threeDSecure = mapped.getThreeDsecure();

        assertNotNull(threeDSecure);
        assertEquals("AUT_uzFr7t4VOqxdLDI44hHmXIjHtOOE8d", threeDSecure.getId());
        assertEquals("SALE", threeDSecure.getTransactionType());
        assertEquals("AVAILABLE", threeDSecure.getStatus());
        assertEquals("CP", threeDSecure.getChannel());
        assertEquals("MER_A6A1EC44522F96630ABEA17A", threeDSecure.getMerchantId());
        assertEquals("transaction_processing", threeDSecure.getAccountName());
        assertEquals("STATIC_CHALLENGE", threeDSecure.getChallengeModel());
        assertEquals("threeDSSessionData", threeDSecure.getSessionDataFieldName());
        assertEquals("string", threeDSecure.getThreeDsMethodReturnUrl());
        assertEquals("string", threeDSecure.getThreeDsServerTransId());
        assertEquals("string", threeDSecure.getEncodedMethodData());
        assertEquals("string", threeDSecure.getChallengeReturnUrl());
        assertEquals("ACT_uzFr7t4VOqxdLDI44hHmXIjHtOOE8d", threeDSecure.getActionId());
        assertEquals("INITIATE", threeDSecure.getActionType());
        assertEquals("SUCCESS", threeDSecure.getActionResultCode());
        assertEquals("uzFr7t4VOqxdLDI44hHmXIjHtOOE8d", threeDSecure.getActionAppId());
        assertEquals("my_lovely_app", threeDSecure.getActionAppName());
    }

    @Test
    public void MapCheck3DSAvailabilityRequest_UsesGpApiChannelValueWhenBuilderChannelIsOmitted() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("transaction_processing");
        config.setAccessTokenInfo(accessTokenInfo);

        GpApiConnector gateway = new GpApiConnector(config);

        CreditCardData card = new CreditCardData();
        card.setNumber("4012001038488884");
        card.setExpMonth(12);
        card.setExpYear(2030);

        Secure3dBuilder builder = Secure3dService.checkEnrollment(card)
                .withAmount(new BigDecimal("10.01"))
                .withCurrency("GBP");

        GpApiRequest request = new com.global.api.builders.requestbuilder.gpApi.GpApiSecureRequestBuilder().buildRequest(builder, gateway);
        JsonDoc requestBody = JsonDoc.parse(request.getRequestBody());

        assertEquals("CNP", requestBody.getString("channel"));
        assertNull(requestBody.get("stored_credential"));
    }

    @Test
    public void MapTransactionSummaryTest_FromObject() throws GatewayException {
        // Arrange
        JsonDoc obj = new JsonDoc();

        obj.set("id", "TRN_TvY1QFXxQKtaFSjNaLnDVdo3PZ7ivz");
        obj.set("time_created", DateTime.now().toString(GpApiConnector.DATE_TIME_DTF));
        obj.set("status", "PREAUTHORIZED");
        obj.set("type", "SALE");
        obj.set("channel", "CNP");
        obj.set("amount", "10000");
        obj.set("currency", "USD");
        obj.set("reference", "My-TRANS-184398775");
        obj.set("batch_id", "BATCH_123456");
        obj.set("country", "US");
        obj.set("parent_resource_id", "PARENT_456123");

        JsonDoc paymentMethod = new JsonDoc();

        paymentMethod.set("message", SUCCESS);
        paymentMethod.set("entry_mode", "ECOM");
        paymentMethod.set("name", "James Mason");

        JsonDoc card = new JsonDoc();

        card.set("brand", "VISA");
        card.set("authcode", "12345");
        card.set("brand_reference", "TQ76bJf7qzkC30U0");
        card.set("arn", "ARN_123456798");
        card.set("masked_number_first6last4", "411111XXXXXX1111");

        paymentMethod.set("card", card);

        obj.set("payment_method", paymentMethod);

        String rawJson = obj.toString();

        // Act
        TransactionSummary transaction = GpApiMapping.mapTransactionSummary(JsonDoc.parse(rawJson));

        // Assert
        assertEquals(obj.getString("id"), transaction.getTransactionId());
        assertEquals(parseGpApiDateTime(obj.getString("time_created")), transaction.getTransactionDate());
        assertEquals(obj.getString("status"), transaction.getTransactionStatus());
        assertEquals(obj.getString("type"), transaction.getTransactionType());
        assertEquals(obj.getString("channel"), transaction.getChannel());
        assertEquals(obj.getAmount("amount"), transaction.getAmount());
        assertEquals(obj.getString("currency"), transaction.getCurrency());
        assertEquals(obj.getString("reference"), transaction.getReferenceNumber());
        assertEquals(obj.getString("reference"), transaction.getClientTransactionId());
        assertEquals(obj.getString("batch_id"), transaction.getBatchSequenceNumber());
        assertEquals(obj.getString("country"), transaction.getCountry());
        assertEquals(obj.getString("parent_resource_id"), transaction.getOriginalTransactionId());
        assertEquals(paymentMethod.getString("message"), transaction.getGatewayResponseMessage());
        assertEquals(paymentMethod.getString("entry_mode"), transaction.getEntryMode());
        assertEquals(paymentMethod.getString("name"), transaction.getCardHolderName());
        assertEquals(card.getString("brand"), transaction.getCardType());
        assertEquals(card.getString("authcode"), transaction.getAuthCode());
        assertEquals(card.getString("brand_reference"), transaction.getBrandReference());
        assertEquals(card.getString("arn"), transaction.getAcquirerReferenceNumber());
        assertEquals(card.getString("masked_number_first6last4"), transaction.getMaskedCardNumber());
    }

    @Test
    public void MapDepositSummaryTest() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"DEP_2342423423\",\"time_created\":\"2020-11-21\",\"status\":\"FUNDED\",\"funding_type\":\"CREDIT\",\"amount\":\"11400\",\"currency\":\"USD\",\"aggregation_model\":\"H-By Date\",\"bank_transfer\":{\"masked_account_number_last4\":\"XXXXXX9999\",\"bank\":{\"code\":\"XXXXX0001\"}},\"system\":{\"mid\":\"101023947262\",\"hierarchy\":\"055-70-024-011-019\",\"name\":\"XYZ LTD.\",\"dba\":\"XYZ Group\"},\"sales\":{\"count\":4,\"amount\":\"12400\"},\"refunds\":{\"count\":1,\"amount\":\"-1000\"},\"discounts\":{\"count\":0,\"amount\":\"\"},\"tax\":{\"count\":0,\"amount\":\"\"},\"disputes\":{\"chargebacks\":{\"count\":0,\"amount\":\"\"},\"reversals\":{\"count\":0,\"amount\":\"\"}},\"fees\":{\"amount\":\"\"},\"action\":{\"id\":\"ACT_TWdmMMOBZ91iQX1DcvxYermuVJ6E6h\",\"type\":\"DEPOSIT_SINGLE\",\"time_created\":\"2020-11-24T18:43:43.370Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"JF2GQpeCrOivkBGsTRiqkpkdKp67Gxi0\",\"app_name\":\"test_app\"}}";

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Act
        DepositSummary deposit = GpApiMapping.mapDepositSummary(doc);

        // Assert
        assertEquals(doc.getString("id"), deposit.getDepositId());
        assertEquals(parseGpApiDate(doc.getString("time_created")), deposit.getDepositDate());
        assertEquals(doc.getString("status"), deposit.getStatus());
        assertEquals(doc.getString("funding_type"), deposit.getType());
        assertEquals(doc.getAmount("amount"), deposit.getAmount());
        assertEquals(doc.getString("currency"), deposit.getCurrency());

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");
            assertEquals(system.getString("mid"), deposit.getMerchantNumber());
            assertEquals(system.getString("hierarchy"), deposit.getMerchantHierarchy());
            assertEquals(system.getString("name"), deposit.getMerchantName());
            assertEquals(system.getString("dba"), deposit.getMerchantDbaName());
        }

        if (doc.has("sales")) {
            JsonDoc sales = doc.get("sales");
            assertEquals((int) sales.getInt("count"), deposit.getSalesTotalCount());
            assertEquals(sales.getAmount("amount"), deposit.getSalesTotalAmount());
        }

        if (doc.has("refunds")) {
            JsonDoc refunds = doc.get("refunds");
            assertEquals((int) refunds.getInt("count"), deposit.getRefundsTotalCount());
            assertEquals(refunds.getAmount("amount"), deposit.getRefundsTotalAmount());
        }

        if (doc.has("disputes")) {
            JsonDoc disputes = doc.get("disputes");
            if (disputes.has("chargebacks")) {
                JsonDoc chargebacks = disputes.get("chargebacks");
                assertEquals((int) chargebacks.getInt("count"), deposit.getChargebackTotalCount());
                assertEquals(chargebacks.getAmount("amount"), deposit.getChargebackTotalAmount());
            }

            if (disputes.has("reversals")) {
                JsonDoc reversals = disputes.get("reversals");
                assertEquals((int) reversals.getInt("count"), deposit.getAdjustmentTotalCount());
                assertEquals(reversals.getAmount("amount"), deposit.getAdjustmentTotalAmount());
            }
        }
        if (doc.has("fees")) {
            assertEquals(doc.get("fees").getAmount("amount"), deposit.getFeesTotalAmount());
        }

    }

    @Test
    public void MapDisputeSummaryTest() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"DIS_SAND_abcd1234\",\"time_created\":\"2020-11-12T18:50:39.721Z\",\"merchant_id\":\"MER_62251730c5574bbcb268191b5f315de8\",\"merchant_name\":\"TEST MERCHANT\",\"account_id\":\"DIA_882c832d13e04185bb6e213d6303ed98\",\"account_name\":\"testdispute\",\"status\":\"WITH_MERCHANT\",\"status_time_created\":\"2020-11-14T18:50:39.721Z\",\"stage\":\"RETRIEVAL\",\"stage_time_created\":\"2020-11-17T18:50:39.722Z\",\"amount\":\"1000\",\"currency\":\"USD\",\"payer_amount\":\"1000\",\"payer_currency\":\"USD\",\"merchant_amount\":\"1000\",\"merchant_currency\":\"USD\",\"reason_code\":\"104\",\"reason_description\":\"Other Fraud-Card Absent Environment\",\"time_to_respond_by\":\"2020-11-29T18:50:39.722Z\",\"result\":\"PENDING\",\"investigator_comment\":\"WITH_MERCHANT RETRIEVAL PENDING 1000 USD 1000 USD\",\"system\":{\"mid\":\"627384967\",\"hierarchy\":\"111-23-099-001-001\",\"name\":\"ABC INC.\"},\"last_adjustment_amount\":\"\",\"last_adjustment_currency\":\"\",\"last_adjustment_funding\":\"\",\"last_adjustment_time_created\":\"2020-11-20T18:50:39.722Z\",\"net_financial_amount\":\"\",\"net_financial_currency\":\"\",\"net_financial_funding\":\"\",\"payment_method_provider\":[{\"comment\":\"issuer comments 34523\",\"reference\":\"issuer-reference-0001\",\"documents\":[{\"id\":\"DOC_MyEvidence_234234AVCDE-1\"}]}],\"transaction\":{\"time_created\":\"2020-10-05T18:50:39.726Z\",\"type\":\"SALE\",\"amount\":\"1000\",\"currency\":\"USD\",\"reference\":\"my-trans-AAA1\",\"remarks\":\"my-trans-AAA1\",\"payment_method\":{\"card\":{\"number\":\"424242xxxxxx4242\",\"arn\":\"834523482349123\",\"brand\":\"VISA\",\"authcode\":\"234AB\",\"brand_reference\":\"23423421342323A\"}}},\"documents\":[],\"action\":{\"id\":\"ACT_5blBTHnIs4aOCIvGwG7KizYUpsGI0g\",\"type\":\"DISPUTE_SINGLE\",\"time_created\":\"2020-11-24T18:50:39.925Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"JF2GQpeCrOivkBGsTRiqkpkdKp67Gxi0\",\"app_name\":\"test_app\"}}";

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Act
        DisputeSummary dispute = GpApiMapping.mapDisputeSummary(doc);

        // Assert
        assertEquals(doc.getString("id"), dispute.getCaseId());
        assertEquals(parseGpApiDateTime(doc.getString("time_created")), dispute.getCaseIdTime());
        assertEquals(doc.getString("status"), dispute.getCaseStatus());
        assertEquals(doc.getString("stage"), dispute.getCaseStage());
        assertEquals(doc.getAmount("amount"), dispute.getCaseAmount());
        assertEquals(doc.getString("currency"), dispute.getCaseCurrency());

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");
            assertEquals(system.getString("mid"), dispute.getCaseMerchantId());
            assertEquals(system.getString("hierarchy"), dispute.getMerchantHierarchy());
        }

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");
                assertEquals(card.getString("number"), dispute.getTransactionMaskedCardNumber());
                assertEquals(card.getString("arn"), dispute.getTransactionARN());
                assertEquals(card.getString("brand"), dispute.getTransactionCardType());
            }
        }

        assertEquals(doc.getString("reason_code"), dispute.getReasonCode());
        assertEquals(doc.getString("reason_description"), dispute.getReason());
        assertEquals(parseGpApiDateTime(doc.getString("time_to_respond_by")), dispute.getRespondByDate());
        assertEquals(doc.getString("result"), dispute.getResult());
        assertEquals(doc.getDecimal("last_adjustment_amount"), dispute.getLastAdjustmentAmount());
        assertEquals(doc.getString("last_adjustment_currency"), dispute.getLastAdjustmentCurrency());
        assertEquals(doc.getString("last_adjustment_funding"), dispute.getLastAdjustmentFunding());
    }

    @Test
    public void MapSettlementDisputeSummaryTest() throws GatewayException {
        String rawJson = "{\"id\":\"DIS_812\",\"status\":\"FUNDED\",\"stage\":\"CHARGEBACK\",\"stage_time_created\":\"2021-03-16T14:03:44\",\"amount\":\"200\",\"currency\":\"GBP\",\"reason_code\":\"PM\",\"reason_description\":\"Paid by Other Means\",\"time_to_respond_by\":\"2021-04-02T14:03:44\",\"result\":\"LOST\",\"funding_type\":\"DEBIT\",\"deposit_time_created\":\"2021-03-20\",\"deposit_id\":\"DEP_2342423443\",\"last_adjustment_amount\":\"\",\"last_adjustment_currency\":\"\",\"last_adjustment_funding\":\"\",\"last_adjustment_time_created\":\"\",\"system\":{\"mid\":\"101023947262\",\"hierarchy\":\"055-70-024-011-019\",\"name\":\"XYZ LTD.\"},\"transaction\":{\"time_created\":\"2021-02-21T14:03:44\",\"merchant_time_created\":\"2021-02-21T16:03:44\",\"type\":\"SALE\",\"amount\":\"200\",\"currency\":\"GBP\",\"reference\":\"28012076eb6M\",\"payment_method\":{\"card\":{\"masked_number_first6last4\":\"379132XXXXX1007\",\"arn\":\"71400011203688701393903\",\"brand\":\"AMEX\",\"authcode\":\"129623\",\"brand_reference\":\"MWE1P0JG80110\"}}}}";

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Act
        DisputeSummary dispute = GpApiMapping.mapSettlementDisputeSummary(doc);

        // Assert
        assertEquals(doc.getString("id"), dispute.getCaseId());
        assertEquals(parseGpApiDateTime(doc.getString("stage_time_created")), dispute.getCaseIdTime());
        assertEquals(doc.getString("status"), dispute.getCaseStatus());
        assertEquals(doc.getString("stage"), dispute.getCaseStage());
        assertEquals(doc.getAmount("amount"), dispute.getCaseAmount());
        assertEquals(doc.getString("currency"), dispute.getCaseCurrency());

        assertEquals(doc.getDecimal("last_adjustment_amount"), dispute.getLastAdjustmentAmount());
        assertEquals(doc.getString("last_adjustment_currency"), dispute.getLastAdjustmentCurrency());
        assertEquals(doc.getString("last_adjustment_funding"), dispute.getLastAdjustmentFunding());

        if (doc.has("system")) {
            JsonDoc system = doc.get("system");

            assertEquals(system.getString("mid"), dispute.getCaseMerchantId());
            assertEquals(system.getString("hierarchy"), dispute.getMerchantHierarchy());
            assertEquals(system.getString("name"), dispute.getMerchantName());
        }

        assertEquals(doc.getString("reason_code"), dispute.getReasonCode());
        assertEquals(doc.getString("reason_description"), dispute.getReason());
        assertEquals(doc.getString("result"), dispute.getResult());

        if (doc.has("transaction")) {
            JsonDoc transaction = doc.get("transaction");

            assertEquals(parseGpApiDateTime(transaction.getString("time_created")), dispute.getTransactionTime());
            assertEquals(transaction.getString("type"), dispute.getTransactionType());
            assertEquals(transaction.getAmount("amount"), dispute.getTransactionAmount());
            assertEquals(transaction.getString("currency"), dispute.getTransactionCurrency());
            assertEquals(transaction.getString("reference"), dispute.getTransactionReferenceNumber());

            if (transaction.has("payment_method")) {
                JsonDoc paymentMethod = transaction.get("payment_method");

                if (paymentMethod.has("card")) {
                    JsonDoc card = paymentMethod.get("card");

                    assertEquals(card.getString("masked_number_first6last4"), dispute.getTransactionMaskedCardNumber());
                    assertEquals(card.getString("arn"), dispute.getTransactionARN());
                    assertEquals(card.getString("brand"), dispute.getTransactionCardType());
                    assertEquals(card.getString("authcode"), dispute.getTransactionAuthCode());
                }
            }
        }

        assertEquals(parseGpApiDateTime(doc.getString("time_to_respond_by")), dispute.getRespondByDate());
        assertEquals(doc.getDate("deposit_time_created", "yyyy-MM-dd"), dispute.getDepositDate());
        assertEquals(doc.getString("deposit_id"), dispute.getDepositReference());
    }

    @Test
    public void MapStoredPaymentMethodSummaryTest() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"PMT_3502a05c-0a79-469b-bff9-994b665ce9d9\",\"time_created\":\"2021-04-23T18:46:57.000Z\",\"status\":\"ACTIVE\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"TKA_eba30a1b5c4a468d90ceeef2ffff7f5e\",\"account_name\":\"Tokenization\",\"reference\":\"faed4ae3-1dd6-414a-bd7e-3a585715d9cc\",\"card\":{\"number_last4\":\"xxxxxxxxxxxx1111\",\"brand\":\"VISA\",\"expiry_month\":\"12\",\"expiry_year\":\"25\"},\"action\":{\"id\":\"ACT_wFGcHivudqleji9jA7S4MTapAHCTkp\",\"type\":\"PAYMENT_METHOD_SINGLE\",\"time_created\":\"2021-04-23T18:47:01.057Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg\",\"app_name\":\"colleens_app\"}}";

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Act
        StoredPaymentMethodSummary paymentMethod = GpApiMapping.mapStoredPaymentMethodSummary(doc);

        // Assert
        assertEquals(doc.getString("id"), paymentMethod.getId());
        assertEquals(parseGpApiDateTime(doc.getString("time_created")), paymentMethod.getTimeCreated());
        assertEquals(doc.getString("status"), paymentMethod.getStatus());
        assertEquals(doc.getString("reference"), paymentMethod.getReference());
        assertEquals(doc.getString("name"), paymentMethod.getName());
        if (doc.has("card")) {
            JsonDoc card = doc.get("card");
            assertEquals(card.getString("number_last4"), paymentMethod.getCardLast4());
            assertEquals(card.getString("brand"), paymentMethod.getCardType());
            assertEquals(card.getString("expiry_month"), paymentMethod.getCardExpMonth());
            assertEquals(card.getString("expiry_year"), paymentMethod.getCardExpYear());
        }
    }

    @Test
    public void MapActionSummaryTest() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"ACT_PJiFWTaNcLW8aVBo2fA8E5Dqd8ZyrH\",\"type\":\"CREATE_TOKEN\",\"time_created\":\"2021-03-24T02:02:27.158Z\",\"resource\":\"ACCESS_TOKENS\",\"resource_parent_id\":\"ACT_PARENT_123\",\"resource_request_url\":\"http://localhost:8998/v7/unifiedcommerce/accesstoken\",\"version\":\"2020-12-22\",\"resource_id\":\"ACT_PJiFWTaNcLW8aVBo2fA8E5Dqd8ZyrH\",\"resource_status\":\"\",\"http_response_code\":\"200\",\"http_response_message\":\"OK\",\"response_code\":\"SUCCESS\",\"response_detailed_code\":\"D001\",\"response_detailed_message\":\"Detailed message\",\"email\":\"colleen.mcgloin@globalpay.com\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"\",\"account_name\":\"\",\"source_location\":\"63.241.252.2\",\"destination_location\":\"74.125.196.153\",\"message_received\":\"Request received\",\"message_sent\":\"Response sent\",\"metrics\":\"X-GP-Version=2020-12-22\",\"total_time_milliseconds\":\"125\",\"total_time_downstream_milliseconds\":\"87\",\"app_id\":\"P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg\",\"app_name\":\"colleens_app\",\"app_developer\":\"colleen.mcgloin@globalpay.com\",\"action\":{\"id\":\"ACT_qOTwHG38UvuWwjcI6DBNu0uqbg8eoR\",\"type\":\"ACTION_SINGLE\",\"time_created\":\"2021-04-23T18:23:05.824Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg\",\"app_name\":\"colleens_app\"}}";

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Act
        ActionSummary action = GpApiMapping.mapActionSummary(doc);

        // Assert
        assertEquals(doc.getString("id"), action.getId());
        assertEquals(doc.getString("type"), action.getType());
        assertEquals(parseGpApiDateTime(doc.getString("time_created")), action.getTimeCreated());
        assertEquals(doc.getString("resource"), action.getResource());
        assertEquals(doc.getString("resource_parent_id"), action.getResourceParentId());
        assertEquals(doc.getString("resource_request_url"), action.getResourceRequestUrl());
        assertEquals(doc.getString("version"), action.getVersion());
        assertEquals(doc.getString("resource_id"), action.getResourceId());
        assertEquals(doc.getString("resource_status"), action.getResourceStatus());
        assertEquals(doc.getString("http_response_code"), action.getHttpResponseCode());
        assertEquals(doc.getString("response_code"), action.getResponseCode());
        assertEquals(doc.getString("response_detailed_code"), action.getResponseDetailedCode());
        assertEquals(doc.getString("response_detailed_message"), action.getResponseDetailedMessage());
        assertEquals(doc.getString("email"), action.getEmail());
        assertEquals(doc.getString("app_id"), action.getAppId());
        assertEquals(doc.getString("app_name"), action.getAppName());
        assertEquals(doc.getString("account_id"), action.getAccountId());
        assertEquals(doc.getString("account_name"), action.getAccountName());
        assertEquals(doc.getString("merchant_name"), action.getMerchantName());
        assertEquals(doc.getString("merchant_id"), action.getMerchantId());
        assertEquals(doc.getString("source_location"), action.getSourceLocation());
        assertEquals(doc.getString("destination_location"), action.getDestinationLocation());
        assertEquals(doc.getString("message_received"), action.getMessageReceived());
        assertEquals(doc.getString("message_sent"), action.getMessageSent());
        assertEquals(doc.getString("metrics"), action.getMetrics());
        assertEquals(doc.getString("total_time_milliseconds"), action.getTotalTimeMilliseconds());
        assertEquals(doc.getString("total_time_downstream_milliseconds"), action.getTotalTimeDownstreamMilliseconds());
    }

    @Test
    public void mapReportResponseFindActionsPagedMapsEnvelopeAndActionRecords() throws ApiException {
        // Arrange
        String rawJson = "{\"total_record_count\":1,\"current_page_size\":1,\"paging\":{\"page\":1,\"page_size\":5,\"order_by\":\"TIME_CREATED\",\"order\":\"ASC\"},\"actions\":[{\"id\":\"ACT_123\",\"type\":\"MERCHANT_EDIT\",\"time_created\":\"2023-05-01T12:00:00.000Z\",\"resource\":\"TRANSACTIONS\",\"resource_parent_id\":\"ACT_PARENT_123\",\"resource_request_url\":\"http://localhost:8998/v7/unifiedcommerce/actions\",\"resource_id\":\"TRN_123\",\"resource_status\":\"DECLINED\",\"version\":\"2021-03-22\",\"http_response_code\":\"400\",\"response_code\":\"DECLINED\",\"response_detailed_code\":\"D001\",\"response_detailed_message\":\"Detailed message\",\"email\":\"test@example.com\",\"merchant_id\":\"MER_123\",\"merchant_name\":\"Sandbox_merchant\",\"account_id\":\"ACC_123\",\"account_name\":\"transaction_processing\",\"source_location\":\"63.241.252.2\",\"destination_location\":\"74.125.196.153\",\"message_received\":\"Request received\",\"message_sent\":\"Response sent\",\"metrics\":\"X-GP-Version=2020-12-22\",\"total_time_milliseconds\":\"125\",\"total_time_downstream_milliseconds\":\"87\",\"app_id\":\"APP_123\",\"app_name\":\"test_app\"}],\"action\":{\"id\":\"ACT_result\",\"type\":\"ACTION_LIST\",\"time_created\":\"2023-05-01T12:01:00.000Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"APP_123\",\"app_name\":\"test_app\"}}";

        // Act
        ActionSummaryPaged result = GpApiMapping.mapReportResponse(rawJson, ReportType.FindActionsPaged);

        // Assert
        assertEquals(1, result.getTotalRecordCount());
        assertEquals(5, result.getPageSize());
        assertEquals(1, result.getPage());
        assertEquals("ASC", result.getOrder());
        assertEquals("TIME_CREATED", result.getOrderBy());
        assertEquals(1, result.getResults().size());

        ActionSummary action = result.getResults().get(0);
        assertEquals("ACT_123", action.getId());
        assertEquals("MERCHANT_EDIT", action.getType());
        assertEquals(parseGpApiDateTime("2023-05-01T12:00:00.000Z"), action.getTimeCreated());
        assertEquals("TRANSACTIONS", action.getResource());
        assertEquals("ACT_PARENT_123", action.getResourceParentId());
        assertEquals("http://localhost:8998/v7/unifiedcommerce/actions", action.getResourceRequestUrl());
        assertEquals("TRN_123", action.getResourceId());
        assertEquals("DECLINED", action.getResourceStatus());
        assertEquals("2021-03-22", action.getVersion());
        assertEquals("400", action.getHttpResponseCode());
        assertEquals("DECLINED", action.getResponseCode());
        assertEquals("D001", action.getResponseDetailedCode());
        assertEquals("Detailed message", action.getResponseDetailedMessage());
        assertEquals("test@example.com", action.getEmail());
        assertEquals("APP_123", action.getAppId());
        assertEquals("test_app", action.getAppName());
        assertEquals("ACC_123", action.getAccountId());
        assertEquals("transaction_processing", action.getAccountName());
        assertEquals("Sandbox_merchant", action.getMerchantName());
        assertEquals("MER_123", action.getMerchantId());
        assertEquals("63.241.252.2", action.getSourceLocation());
        assertEquals("74.125.196.153", action.getDestinationLocation());
        assertEquals("Request received", action.getMessageReceived());
        assertEquals("Response sent", action.getMessageSent());
        assertEquals("X-GP-Version=2020-12-22", action.getMetrics());
        assertEquals("125", action.getTotalTimeMilliseconds());
        assertEquals("87", action.getTotalTimeDownstreamMilliseconds());
    }

    @Test
    public void MapResponseTest_CreateTransaction() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"TRN_BHZ1whvNJnMvB6dPwf3znwWTsPjCn0\",\"time_created\":\"2020-12-04T12:46:05.235Z\",\"type\":\"SALE\",\"status\":\"PREAUTHORIZED\",\"channel\":\"CNP\",\"capture_mode\":\"LATER\",\"amount\":\"1400\",\"currency\":\"USD\",\"country\":\"US\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"TRA_6716058969854a48b33347043ff8225f\",\"account_name\":\"Transaction_Processing\",\"reference\":\"15fbcdd9-8626-4e29-aae8-050f823f995f\",\"payment_method\":{\"id\":\"PMT_9a8f1b66-58e3-409d-86df-ed5fb14ad2f6\",\"result\":\"00\",\"message\":\"[ test system ] AUTHORISED\",\"entry_mode\":\"ECOM\",\"fingerprint\":\"\",\"fingerprint_presence_indicator\":\"\",\"card\":{\"funding\":\"CREDIT\",\"brand\":\"VISA\",\"masked_number_last4\":\"XXXXXXXXXXXX5262\",\"authcode\":\"12345\",\"brand_reference\":\"PSkAnccWLNMTcRmm\",\"brand_time_created\":\"\",\"cvv_result\":\"MATCHED\"}},\"batch_id\":\"\",\"action\":{\"id\":\"ACT_BHZ1whvNJnMvB6dPwf3znwWTsPjCn0\",\"type\":\"PREAUTHORIZE\",\"time_created\":\"2020-12-04T12:46:05.235Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"Uyq6PzRbkorv2D4RQGlldEtunEeGNZll\",\"app_name\":\"sample_app_CERT\"}}";

        // Act
        Transaction transaction = GpApiMapping.mapResponse(rawJson);

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Assert
        assertEquals(doc.getString("id"), transaction.getTransactionId());
        assertEquals(0, doc.getAmount("amount").compareTo(transaction.getBalanceAmount()));
        assertEquals(doc.getString("time_created"), transaction.getTimestamp());
        assertEquals(doc.getString("status"), transaction.getResponseMessage());
        assertEquals(doc.getString("reference"), transaction.getReferenceNumber());
        assertEquals(doc.getString("batch_id"), transaction.getBatchSummary().getBatchReference());
        assertEquals(doc.get("action").getString("result_code"), transaction.getOriginalResponseCode());
        assertEquals("00", transaction.getResponseCode());
        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            assertEquals(paymentMethod.getString("id"), transaction.getToken());
            assertEquals(paymentMethod.getString("result"), transaction.getCardIssuerResponse().getResult());
            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");
                assertEquals(card.getString("brand"), transaction.getCardType());
                assertEquals(card.getString("masked_number_last4"), transaction.getCardLast4());
                assertEquals(card.getString("cvv_result"), transaction.getCvnResponseMessage());
            }
        }
    }

    @Test
    public void MapResponseTest_CreateTransaction_withAvsData() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"TRN_J7ocSiyeHOJ1XK1jHjg9hq9U5nS0Nz_057d45d5f1fc\",\"time_created\":\"2021-09-01T14:27:41.713Z\",\"type\":\"SALE\",\"status\":\"CAPTURED\",\"channel\":\"CNP\",\"capture_mode\":\"AUTO\",\"amount\":\"1999\",\"currency\":\"USD\",\"country\":\"US\",\"merchant_id\":\"MER_7e3e2c7df34f42819b3edee31022ee3f\",\"merchant_name\":\"Sandbox_merchant_3\",\"account_id\":\"TRA_c9967ad7d8ec4b46b6dd44a61cde9a91\",\"account_name\":\"transaction_processing\",\"reference\":\"4d361180-304a-4f8a-9e82-057d45d5f1fc\",\"payment_method\":{\"result\":\"00\",\"message\":\"[ test system ] AUTHORISED\",\"entry_mode\":\"ECOM\",\"fingerprint\":\"\",\"fingerprint_presence_indicator\":\"\",\"card\":{\"funding\":\"CREDIT\",\"brand\":\"VISA\",\"masked_number_last4\":\"XXXXXXXXXXXX5262\",\"authcode\":\"12345\",\"brand_reference\":\"vQBOsL3WUjuaaEmT\",\"brand_time_created\":\"\",\"cvv_result\":\"MATCHED\",\"avs_address_result\":\"MATCHED\",\"avs_postal_code_result\":\"MATCHED\",\"avs_action\":\"\",\"provider\":{\"result\":\"00\",\"cvv_result\":\"M\",\"avs_address_result\":\"M\",\"avs_postal_code_result\":\"M\"}}},\"batch_id\":\"BAT_983471\",\"action\":{\"id\":\"ACT_J7ocSiyeHOJ1XK1jHjg9hq9U5nS0Nz\",\"type\":\"AUTHORIZE\",\"time_created\":\"2021-09-01T14:27:41.713Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"rkiYguPfTurmGcVhkDbIGKn2IJe2t09M\",\"app_name\":\"sample_app_CERT\"}}";

        // Act
        Transaction transaction = GpApiMapping.mapResponse(rawJson);

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Assert
        assertEquals(doc.getString("id"), transaction.getTransactionId());
        assertEquals(0, StringUtils.toAmount(doc.getString("amount")).compareTo(transaction.getBalanceAmount()));
        assertEquals(doc.getString("time_created"), transaction.getTimestamp());
        assertEquals(doc.getString("status"), transaction.getResponseMessage());
        assertEquals(doc.getString("reference"), transaction.getReferenceNumber());
        assertEquals(doc.getString("batch_id"), transaction.getBatchSummary().getBatchReference());
        assertEquals(doc.get("action").getString("result_code"), transaction.getOriginalResponseCode());
        assertEquals("00", transaction.getResponseCode());

        var payment_method = doc.get("payment_method");
        if (payment_method != null) {
            assertEquals(payment_method.getString("id"), transaction.getToken());
            assertEquals(payment_method.getString("result"), transaction.getCardIssuerResponse().getResult());

            var card = payment_method.get("card");
            if (card != null) {
                assertEquals(card.getString("brand"), transaction.getCardType());
                assertEquals(card.getString("masked_number_last4"), transaction.getCardLast4());
                assertEquals(card.getString("cvv_result"), transaction.getCvnResponseMessage());
                assertEquals(card.getString("avs_postal_code_result"), transaction.getAvsResponseCode());
                assertEquals(card.getString("avs_address_result"), transaction.getAvsAddressResponse());
                assertEquals(card.getString("avs_action"), transaction.getAvsResponseMessage());
            }
        }
    }

    @Test
    public void MapResponseTest_BatchClose() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"BAT_631762-460\",\"time_last_updated\":\"2021-04-23T18:54:52.467Z\",\"status\":\"CLOSED\",\"amount\":\"869\",\"currency\":\"USD\",\"country\":\"US\",\"transaction_count\":2,\"action\":{\"id\":\"ACT_QUuw7OPd9Rw8n72oaVOmVlQXpuhLUZ\",\"type\":\"CLOSE\",\"time_created\":\"2021-04-23T18:54:52.467Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg\",\"app_name\":\"colleens_app\"}}";

        // Act
        Transaction transaction = GpApiMapping.mapResponse(rawJson);

        // Assert
        assertNotNull(transaction.getBatchSummary());
        JsonDoc doc = JsonDoc.parse(rawJson);
        assertEquals(transaction.getBatchSummary().getBatchReference(), doc.getString("id"));
        assertEquals(transaction.getBatchSummary().getStatus(), doc.getString("status"));
        assertEquals(transaction.getBatchSummary().getTotalAmount(), doc.getAmount("amount"));
        assertEquals(transaction.getBatchSummary().getTransactionCount(), doc.getInt("transaction_count"));
    }

    @Test
    public void MapResponseTest_CreateStoredPaymentMethod() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"PMT_e150ba7c-bbbd-41fe-bc04-f21d18def2a1\",\"time_created\":\"2021-04-26T14:59:00.813Z\",\"status\":\"ACTIVE\",\"usage_mode\":\"MULTIPLE\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"TKA_eba30a1b5c4a468d90ceeef2ffff7f5e\",\"account_name\":\"Tokenization\",\"reference\":\"9486a9e8-d8bd-4fd2-877c-796d07f3a2ce\",\"card\":{\"masked_number_last4\":\"XXXXXXXXXXXX1111\",\"brand\":\"VISA\",\"expiry_month\":\"12\",\"expiry_year\":\"25\"},\"action\":{\"id\":\"ACT_jFOurWcX9CvA8UKtEywVpxArNEryvZ\",\"type\":\"PAYMENT_METHOD_CREATE\",\"time_created\":\"2021-04-26T14:59:00.813Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg\",\"app_name\":\"colleens_app\"}}";

        // Act
        Transaction transaction = GpApiMapping.mapResponse(rawJson);

        // Assert
        JsonDoc doc = JsonDoc.parse(rawJson);
        assertEquals(transaction.getToken(), doc.getString("id"));
        assertEquals(transaction.getTimestamp(), doc.getString("time_created"));
        assertEquals(transaction.getReferenceNumber(), doc.getString("reference"));
        assertEquals(transaction.getCardType(), doc.get("card").getString("brand"));
        assertEquals(transaction.getCardNumber(), doc.get("card").getString("number"));
        assertEquals(transaction.getCardLast4(), doc.get("card").getString("masked_number_last4"));
        assertEquals(transaction.getCardExpMonth(), doc.get("card").getInt("expiry_month").intValue());
        assertEquals(transaction.getCardExpYear(), doc.get("card").getInt("expiry_year").intValue());
    }

    @Test
    public void MapMerchantAccountSummary_MapsPagingAndTimestamps() throws GatewayException, UnsupportedTransactionException {
        String rawJson = "{"
                + "\"id\":\"FMA_123\","
                + "\"type\":\"FUND_MANAGEMENT\","
                + "\"time_created\":\"2023-04-27T21:23:39.718Z\","
                + "\"time_last_updated\":\"2023-04-28T21:23:39.718Z\","
                + "\"balances\":{\"available_balance\":\"1000\",\"pending_balance\":\"25\"},"
                + "\"action\":{\"type\":\"ACCOUNTS_SINGLE\",\"result_code\":\"SUCCESS\"}}";

        MerchantAccountSummary accountSummary = GpApiMapping.mapMerchantEndpointResponse(rawJson);

        assertEquals(parseGpApiDateTime("2023-04-27T21:23:39.718Z"), accountSummary.getTimeCreated());
        assertEquals(parseGpApiDateTime("2023-04-28T21:23:39.718Z"), accountSummary.getTimeUpdated());
        assertEquals(new BigDecimal("1000"), accountSummary.getBalances().getAvailableBalance());
        assertEquals(new BigDecimal("25"), accountSummary.getBalances().getPendingBalance());
    }

    @Test
    public void MapMerchantAccountSummary_SingleValueCollectionsTest() throws GatewayException, UnsupportedTransactionException {
        String rawJson = "{\"id\":\"TRA_123\",\"type\":\"TRANSACTION_PROCESSING\",\"name\":\"transaction_processing\",\"status\":\"ACTIVE\",\"permissions\":[\"APP_GET_Single_Key\"],\"countries\":[\"US\"],\"channels\":\"CP\",\"currencies\":[\"USD\"],\"payment_methods\":\"CARD\",\"configurations\":[{\"countries\":[\"US\"],\"currencies\":[\"USD\"],\"channels\":\"CP\",\"payment_methods\":\"CARD\"}],\"action\":{\"id\":\"ACT_uzFr7t4VOqxdLDI44hHmXIjHtOOE8d\",\"type\":\"ACCOUNTS_SINGLE\",\"time_created\":\"2021-05-03T21:23:39.718Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"U1lRHKomEn7DN907RCDPxVhyMfiMLcfy\",\"app_name\":\"demo_app\"}}";

        MerchantAccountSummary accountSummary = GpApiMapping.mapMerchantEndpointResponse(rawJson);

        assertNotNull(accountSummary.getChannels());
        assertEquals(Channel.CardPresent, accountSummary.getChannels().get(0));
        assertNotNull(accountSummary.getPaymentMethods());
        assertEquals(PaymentMethodName.Card, accountSummary.getPaymentMethods().get(0));
        assertNotNull(accountSummary.getConfigurationDetails());
        assertEquals(Channel.CardPresent, accountSummary.getConfigurationDetails().get(0).getChannels().get(0));
        assertEquals(PaymentMethodName.Card, accountSummary.getConfigurationDetails().get(0).getPaymentMethods().get(0));
    }

    @Test
    public void MapInitiate3DSRequest_IncludesEnhancedAuthenticationFields() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("Transaction_Processing");
        config.setAccessTokenInfo(accessTokenInfo);
        config.setMerchantContactUrl("https://merchant.example/contact");

        GpApiConnector gateway = new GpApiConnector(config);
        gateway.setChallengeNotificationUrl("https://challenge.example/return");
        gateway.setMethodNotificationUrl("https://method.example/return");

        CreditCardData card = new CreditCardData();
        card.setNumber("4012001038488884");
        card.setExpMonth(12);
        card.setExpYear(2030);

        Secure3dBuilder builder = Secure3dService
                .initiateAuthentication(card, null)
                .withServerTransactionId("AUT_auth_123")
                .withAmount(new BigDecimal("50.89"))
                .withCurrency("EUR")
                .withReferenceNumber("3ds-initiate-ref")
                .withOrderCreateDate(new DateTime(2021, 5, 3, 21, 23, 39, 718, DateTimeZone.forOffsetHoursMinutes(5, 30)))
                .withChallengeNotificationUrl("https://challenge.example/return")
                .withMethodNotificationUrl("https://method.example/return")
                .withMethodUrlCompletion(com.global.api.entities.enums.MethodUrlCompletion.Yes)
                .withMessageVersion(MessageVersion.Version_210)
                .withChallengeRequestIndicator(com.global.api.entities.enums.ChallengeRequestIndicator.ChallengeMandated)
                .withMerchantInitiatedRequestType(com.global.api.entities.enums.MerchantInitiatedRequestType.OtherPayment)
                .withWhitelistStatus(true)
                .withEnableExemptionOptimization(true)
                .withChallengeWindowSize("FULL_SCREEN")
                .withDeviceRenderingOptions("BOTH")
                .withFrictionlessFlowEligible(true)
                .withExemptionRequestType("LOW_VALUE")
                .withChallengeType("STATIC")
                .withDecoupledFlowIndicator("SUPPORTED")
                .withDecoupledFlowTimerValue(10)
                .withDecoupledFlowTimerUnit("MINUTES")
                .withDecoupledNotificationUrl("https://decoupled.example/notify")
                .withNotificationUrl("https://notify.example/3ds")
                .withRiskScore("87")
                .withRiskAssessmentStatus("LOW")
                .withTrustedMerchantExemption("ALLOWED")
                .withLowValueExemption("ALLOWED")
                .withTransactionRiskAnalysisExemption("ALLOWED")
                .withRecurringExemption("NOT_REQUESTED");

        GpApiRequest request = new com.global.api.builders.requestbuilder.gpApi.GpApiSecureRequestBuilder().buildRequest(builder, gateway);
        JsonDoc requestBody = JsonDoc.parse(request.getRequestBody());

        assertTrue(request.getEndpoint().endsWith("/authentications/AUT_auth_123/initiate"));
        assertEquals("PAYMENT_TRANSACTION", requestBody.get("three_ds").getString("authentication_request_type"));
        assertNull(requestBody.get("three_ds").getString("message_category"));
        assertEquals(MessageVersion.Version_210.getValue(), requestBody.get("three_ds").getString("message_version"));
        assertEquals("CHALLENGE_MANDATED", requestBody.get("three_ds").getString("preference"));
        assertEquals("FULL_SCREEN", requestBody.get("three_ds").getString("challenge_window_size"));
        assertEquals("BOTH", requestBody.get("three_ds").getString("device_rendering_options"));
        assertEquals(Boolean.TRUE, requestBody.get("three_ds").getBool("frictionless_flow_eligible"));
        assertEquals("LOW_VALUE", requestBody.get("three_ds").getString("exemption_request_type"));
        assertEquals("STATIC", requestBody.get("three_ds").getString("challenge_type"));
        assertEquals("2021-05-03T15:53:39.718Z", requestBody.get("order").getString("time_created_reference"));
        assertEquals("SUPPORTED", requestBody.get("three_ds").getString("decoupled_flow_indicator"));
        assertEquals(10, requestBody.get("three_ds").getInt("decoupled_flow_timer_value").intValue());
        assertEquals("MINUTES", requestBody.get("three_ds").getString("decoupled_flow_timer_unit"));
        assertEquals("https://decoupled.example/notify", requestBody.get("notifications").getString("decoupled_notification_url"));
        assertEquals("https://notify.example/3ds", requestBody.get("three_ds").getString("notification_url"));
        assertEquals("87", requestBody.get("three_ds").getString("risk_score"));
        assertEquals("LOW", requestBody.get("three_ds").getString("risk_assessment_status"));
        assertEquals("ALLOWED", requestBody.get("three_ds").getString("trusted_merchant_exemption"));
        assertEquals("ALLOWED", requestBody.get("three_ds").getString("low_value_exemption"));
        assertEquals("ALLOWED", requestBody.get("three_ds").getString("transaction_risk_analysis_exemption"));
        assertEquals("NOT_REQUESTED", requestBody.get("three_ds").getString("recurring_exemption"));
        assertEquals("OTHER_PAYMENT", requestBody.getString("merchant_initiated_request_type"));
        assertEquals(Boolean.TRUE, requestBody.getBool("whitelist_status"));
        assertEquals(Boolean.TRUE, requestBody.getBool("enable_exemption_optimization"));
    }

    @Test
    public void MapGet3DSResultRequest_BuildsChallengeResultPayload() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        GpApiConnector gateway = new GpApiConnector(config);

        Secure3dBuilder builder = Secure3dService
                .getAuthenticationData()
                .withServerTransactionId("AUT_result_123")
                .withPayerAuthenticationResponse("challenge-result-token");

        GpApiRequest request = new com.global.api.builders.requestbuilder.gpApi.GpApiSecureRequestBuilder().buildRequest(builder, gateway);
        JsonDoc requestBody = JsonDoc.parse(request.getRequestBody());

        assertTrue(request.getEndpoint().endsWith("/authentications/AUT_result_123/result"));
        assertEquals("challenge-result-token", requestBody.get("three_ds").getString("challenge_result_value"));
    }

    @Test
    public void MapAuthenticationReportRequestAndPagedResponseTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        GpApiConnector gateway = new GpApiConnector(config);

        var builder = ReportingService.findAuthenticationsPaged(2, 25)
                .orderBy(AuthenticationSortProperty.TimeCreated, SortDirection.Descending)
                .withAuthenticationId("AUT_list_123")
                .withAuthenticationStatus("SUCCESS_AUTHENTICATED");

        builder.where(SearchCriteria.AccountName, "Transaction_Processing")
                .and(SearchCriteria.AccountId, "TRA_123")
                .and(SearchCriteria.ReferenceNumber, "auth-ref-123")
                .and(SearchCriteria.StartDate, new DateTime(2021, 5, 1, 0, 0).toDate())
                .and(SearchCriteria.EndDate, new DateTime(2021, 5, 31, 0, 0).toDate())
                .and(SearchCriteria.Country, "US")
                .and(SearchCriteria.AuthenticationSource, com.global.api.entities.enums.AuthenticationSource.Browser)
                .and(SearchCriteria.AuthenticationType, com.global.api.entities.enums.AuthenticationRequestType.PaymentTransaction)
                .and(SearchCriteria.AuthenticationMessageCategory, MessageCategory.PaymentAuthentication)
                .and(SearchCriteria.AuthenticationMessageVersion, MessageVersion.Version_210);

        GpApiRequest request = new GpApiReportRequestBuilder().buildRequest(builder, gateway);

        assertTrue(request.getEndpoint().endsWith("/authentications"));
        assertEquals("2", request.getQueryStringParams().get("page"));
        assertEquals("25", request.getQueryStringParams().get("page_size"));
        assertEquals("TIME_CREATED", request.getQueryStringParams().get("order_by"));
        assertEquals("DESC", request.getQueryStringParams().get("order"));
        assertEquals("AUT_list_123", request.getQueryStringParams().get("id"));
        assertEquals("SUCCESS_AUTHENTICATED", request.getQueryStringParams().get("status"));
        assertEquals("Transaction_Processing", request.getQueryStringParams().get("account_name"));
        assertEquals("TRA_123", request.getQueryStringParams().get("account_id"));
        assertEquals("auth-ref-123", request.getQueryStringParams().get("reference"));
        assertEquals("US", request.getQueryStringParams().get("country"));
        assertEquals("2021-05-01", request.getQueryStringParams().get("from_time_created"));
        assertEquals("2021-05-31", request.getQueryStringParams().get("to_time_created"));
        assertEquals("BROWSER", request.getQueryStringParams().get("authentication_source"));

        String rawJson = "{\"total_count\":1,\"paging\":{\"page\":2,\"page_size\":25,\"order\":\"DESC\",\"order_by\":\"TIME_CREATED\"},\"authentications\":[{\"id\":\"AUT_list_123\",\"time_created\":\"2021-05-03T21:23:39.718Z\",\"status\":\"SUCCESS_AUTHENTICATED\",\"transaction_type\":\"SALE\",\"channel\":\"CNP\",\"amount\":\"11099\",\"currency\":\"USD\",\"reference\":\"auth-ref-123\",\"country\":\"US\",\"merchant_id\":\"MER_123\",\"merchant_name\":\"ABC INDUSTRIES\",\"account_id\":\"TRA_123\",\"account_name\":\"Transaction_Processing\",\"three_ds\":{\"server_trans_ref\":\"server-trans-ref\",\"message_version\":\"2.1.0\",\"authentication_source\":\"BROWSER\",\"authentication_request_type\":\"PAYMENT_TRANSACTION\",\"message_category\":\"PAYMENT_AUTHENTICATION\",\"liability_shift\":\"YES\"}}]}";

        AuthenticationSummaryPaged paged = GpApiMapping.mapReportResponse(rawJson, ReportType.FindAuthenticationsPaged);

        assertEquals(1, paged.getResults().size());
        assertEquals(1, paged.getTotalRecordCount());
        assertEquals(2, paged.getPage());
        assertEquals(25, paged.getPageSize());
        assertEquals("AUT_list_123", paged.getResults().get(0).getId());
        assertEquals("SUCCESS_AUTHENTICATED", paged.getResults().get(0).getStatus());
        assertEquals("USD", paged.getResults().get(0).getCurrency());
        assertEquals("US", paged.getResults().get(0).getCountry());
        assertEquals("BROWSER", paged.getResults().get(0).getAuthenticationSource());
        assertEquals("PAYMENT_TRANSACTION", paged.getResults().get(0).getAuthenticationType());
        assertEquals("YES", paged.getResults().get(0).getLiabilityShift());
    }

    @Test
    public void MapAuthenticationDetailResponseTest() throws ApiException {
        String rawJson = "{\"id\":\"AUT_detail_123\",\"time_created\":\"2021-05-03T21:23:39.718Z\",\"transaction_type\":\"SALE\",\"status\":\"SUCCESS_AUTHENTICATED\",\"channel\":\"CNP\",\"amount\":\"11099\",\"currency\":\"USD\",\"country\":\"AD\",\"merchant_id\":\"MER_A6A1EC44522F96630ABEA17A\",\"merchant_name\":\"ABC INDUSTRIES\",\"account_id\":\"TRA_86920f927028745yt34d077d88beb29b\",\"account_name\":\"transaction_processing\",\"reference\":\"becf9f3e-4d33-459c-8ed2-0c4affc9555e\",\"payment_method\":{\"result\":\"00\",\"message\":\"SUCCESS\",\"entry_mode\":\"ECOM\",\"name\":\"Jane Doe\",\"card\":{\"brand\":\"VISA\",\"masked_number_first6last4\":\"411111XXXXXX1111\",\"masked_number_last4\":\"XXXXXXXXXXXX1111\",\"expiry_month\":\"12\",\"expiry_year\":\"30\",\"authcode\":\"12345\",\"brand_reference\":\"TQ76bJf7qzkC30U0\",\"funding\":\"CREDIT\"}},\"three_ds\":{\"server_trans_ref\":\"server-trans-ref\",\"message_version\":\"2.1.0\",\"ds_protocol_version_start\":\"2.1.0\",\"ds_protocol_version_end\":\"2.2.0\",\"acs_protocol_version_start\":\"2.1.0\",\"acs_protocol_version_end\":\"2.2.0\",\"acs_info_indicator\":[\"NOT_AVAILABLE\"],\"enrolled_status\":\"ENROLLED\",\"eci\":\"05\",\"cavv_result\":\"MATCHED\",\"liability_shift\":\"YES\",\"challenge_model\":\"STATIC_CHALLENGE\",\"session_data_field_name\":\"threeDSSessionData\",\"message_type\":\"PAREQ\",\"challenge_value\":\"challenge-data\",\"redirect_url\":\"https://redirect.example\",\"acs_challenge_request_url\":\"https://acs.example/challenge\",\"method_url\":\"https://acs.example/method\",\"challenge_window_size\":\"FULL_SCREEN\",\"frictionless_flow_eligible\":true,\"exempt_status\":\"TRANSACTION_RISK_ANALYSIS\",\"eos_reason\":\"LOW_VALUE_EXEMPTION\",\"exemption_request_type\":\"LOW_VALUE\",\"exemption_approval_url\":\"https://approval.example\",\"exemption_rejection_reason\":\"NONE\",\"challenge_type\":\"STATIC\",\"creq_data\":\"creq-data\",\"acs_signed_content\":\"signed-content\",\"decoupled_flow_indicator\":\"SUPPORTED\",\"decoupled_flow_timer_value\":10,\"decoupled_flow_timer_unit\":\"MINUTES\",\"notification_url\":\"https://notify.example/3ds\",\"hardcoded_decision\":true,\"risk_score\":\"87\",\"risk_assessment_status\":\"LOW\",\"trusted_merchant_exemption\":\"ALLOWED\",\"low_value_exemption\":\"ALLOWED\",\"transaction_risk_analysis_exemption\":\"ALLOWED\",\"recurring_exemption\":\"NOT_REQUESTED\",\"method_url_completion_indicator\":\"YES\",\"authentication_request_data\":\"request-data\",\"authentication_response_data\":\"response-data\",\"authentication_value\":\"auth-value\",\"authentication_source\":\"BROWSER\",\"authentication_request_type\":\"PAYMENT_TRANSACTION\",\"message_category\":\"PAYMENT_AUTHENTICATION\",\"status_reason\":\"AUTHENTICATION_SUCCESSFUL\",\"method_data\":{\"three_ds_server_trans_id\":\"3ds-server-trans-id\",\"three_ds_method_return_url\":\"https://method.example/return\",\"encoded_method_data\":\"encoded-method-data\"}},\"notifications\":{\"challenge_return_url\":\"https://challenge.example/return\"},\"action\":{\"id\":\"ACT_detail_123\",\"type\":\"RESULT\",\"time_created\":\"2021-05-03T21:23:39.718Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"app-id\",\"app_name\":\"my_lovely_app\"}}";

        ThreeDSecure threeDSecure = GpApiMapping.mapReportResponse(rawJson, ReportType.AuthenticationDetail);

        assertNotNull(threeDSecure);
        assertEquals("AUT_detail_123", threeDSecure.getId());
        assertEquals("AD", threeDSecure.getCountry());
        assertEquals("SUCCESS", threeDSecure.getPaymentMethodMessage());
        assertEquals("Jane Doe", threeDSecure.getPaymentMethodName());
        assertNotNull(threeDSecure.getCard());
        assertEquals("VISA", threeDSecure.getCard().getBrand());
        assertEquals("411111XXXXXX1111", threeDSecure.getCard().getMaskedCardNumber());
        assertEquals("XXXXXXXXXXXX1111", threeDSecure.getCard().getMaskedNumberLast4());
        assertEquals("FULL_SCREEN", threeDSecure.getChallengeWindowSize());
        assertEquals(Boolean.TRUE, threeDSecure.getFrictionlessFlowEligible());
        assertEquals("LOW_VALUE", threeDSecure.getExemptionRequestType());
        assertEquals("https://approval.example", threeDSecure.getExemptionApprovalURL());
        assertEquals("NONE", threeDSecure.getExemptionRejectionReason());
        assertEquals("STATIC", threeDSecure.getChallengeType());
        assertEquals("creq-data", threeDSecure.getCreqData());
        assertEquals("signed-content", threeDSecure.getAcsSignedContent());
        assertEquals("SUPPORTED", threeDSecure.getDecoupledFlowIndicator());
        assertEquals(10, threeDSecure.getDecoupledFlowTimerValue().intValue());
        assertEquals("MINUTES", threeDSecure.getDecoupledFlowTimerUnit());
        assertEquals("https://notify.example/3ds", threeDSecure.getNotificationURL());
        assertEquals(Boolean.TRUE, threeDSecure.getHardcodedDecision());
        assertEquals("87", threeDSecure.getRiskScore());
        assertEquals("LOW", threeDSecure.getRiskAssessmentStatus());
        assertEquals("ALLOWED", threeDSecure.getTrustedMerchantExemption());
        assertEquals("ALLOWED", threeDSecure.getLowValueExemption());
        assertEquals("ALLOWED", threeDSecure.getTransactionRiskAnalysisExemption());
        assertEquals("NOT_REQUESTED", threeDSecure.getRecurringExemption());
        assertEquals("YES", threeDSecure.getMethodUrlCompletionIndicator());
        assertEquals("request-data", threeDSecure.getAuthenticationRequestData());
        assertEquals("response-data", threeDSecure.getAuthenticationResponseData());
        assertEquals("https://challenge.example/return", threeDSecure.getChallengeReturnUrl());
        assertEquals("3ds-server-trans-id", threeDSecure.getThreeDsServerTransId());
        assertEquals("https://method.example/return", threeDSecure.getThreeDsMethodReturnUrl());
        assertEquals("encoded-method-data", threeDSecure.getEncodedMethodData());
        assertEquals("MATCHED", threeDSecure.getCavv());
        assertEquals(com.global.api.entities.enums.ExemptStatus.TransactionRiskAnalysis, threeDSecure.getExemptStatus());
        assertEquals("LOW_VALUE_EXEMPTION", threeDSecure.getExemptReason());
        assertEquals("AUTHENTICATION_SUCCESSFUL", threeDSecure.getStatusReason());
        assertEquals("PAYMENT_AUTHENTICATION", threeDSecure.getMessageCategory());
        assertEquals("PAYMENT_TRANSACTION", threeDSecure.getAuthenticationType());
        assertEquals("BROWSER", threeDSecure.getAuthenticationSource());
        assertEquals("ACT_detail_123", threeDSecure.getActionId());
    }

    @Test
    public void MapAuthenticationReportRequestWithThreeDsFiltersTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        GpApiConnector gateway = new GpApiConnector(config);

        var builder = ReportingService.findAuthenticationsPaged(1, 25)
                .orderBy(AuthenticationSortProperty.TimeCreated, SortDirection.Descending)
                .withAuthenticationId("AUT_list_123")
                .withAuthenticationStatus("SUCCESS_AUTHENTICATED");

        builder.where(SearchCriteria.ThreeDsDsTransRef, "DS_123456")
                .and(SearchCriteria.ThreeDsServerTransRef, "server-trans-ref")
                .and(SearchCriteria.ThreeDsLiabilityShift, "YES")
                .and(SearchCriteria.ThreeDsAuthenticationValue, "auth-value")
                .and(SearchCriteria.ThreeDsEci, "05")
                .and(SearchCriteria.ThreeDsMessageVersion, "2.1.0")
                .and(SearchCriteria.Source, "BROWSER");

        GpApiRequest request = new GpApiReportRequestBuilder().buildRequest(builder, gateway);

        assertTrue(request.getEndpoint().endsWith("/authentications"));
        assertEquals("1", request.getQueryStringParams().get("page"));
        assertEquals("25", request.getQueryStringParams().get("page_size"));
        assertEquals("AUT_list_123", request.getQueryStringParams().get("id"));
        assertEquals("SUCCESS_AUTHENTICATED", request.getQueryStringParams().get("status"));
        assertEquals("DS_123456", request.getQueryStringParams().get("three_ds.ds_trans_ref"));
        assertEquals("server-trans-ref", request.getQueryStringParams().get("three_ds.server_trans_ref"));
        assertEquals("YES", request.getQueryStringParams().get("three_ds.liability_shift"));
        assertEquals("auth-value", request.getQueryStringParams().get("three_ds.authentication_value"));
        assertEquals("05", request.getQueryStringParams().get("three_ds.eci"));
        assertEquals("2.1.0", request.getQueryStringParams().get("three_ds.message_version"));
        assertEquals("BROWSER", request.getQueryStringParams().get("source"));
    }

}
