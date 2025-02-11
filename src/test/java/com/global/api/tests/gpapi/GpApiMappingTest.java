package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.ActionSummary;
import com.global.api.entities.reporting.DepositSummary;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.entities.reporting.StoredPaymentMethodSummary;
import com.global.api.gateways.GpApiConnector;
import com.global.api.mapping.GpApiMapping;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
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
        String rawJson = "{\"id\":\"ACT_PJiFWTaNcLW8aVBo2fA8E5Dqd8ZyrH\",\"type\":\"CREATE_TOKEN\",\"time_created\":\"2021-03-24T02:02:27.158Z\",\"resource\":\"ACCESS_TOKENS\",\"resource_request_url\":\"http://localhost:8998/v7/unifiedcommerce/accesstoken\",\"version\":\"2020-12-22\",\"resource_parent_id\":\"\",\"resource_id\":\"ACT_PJiFWTaNcLW8aVBo2fA8E5Dqd8ZyrH\",\"resource_status\":\"\",\"http_response_code\":\"200\",\"http_response_message\":\"OK\",\"response_code\":\"SUCCESS\",\"response_detailed_code\":\"\",\"response_detailed_message\":\"\",\"app_id\":\"P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg\",\"app_name\":\"colleens_app\",\"app_developer\":\"colleen.mcgloin@globalpay.com\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"\",\"account_name\":\"\",\"source_location\":\"63.241.252.2\",\"destination_location\":\"74.125.196.153\",\"metrics\":{\"X-GP-Version\":\"2020-12-22\"},\"action\":{\"id\":\"ACT_qOTwHG38UvuWwjcI6DBNu0uqbg8eoR\",\"type\":\"ACTION_SINGLE\",\"time_created\":\"2021-04-23T18:23:05.824Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"P3LRVjtGRGxWQQJDE345mSkEh2KfdAyg\",\"app_name\":\"colleens_app\"}}";

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Act
        ActionSummary action = GpApiMapping.mapActionSummary(doc);

        // Assert
        assertEquals(doc.getString("id"), action.getId());
        assertEquals(doc.getString("type"), action.getType());
        assertEquals(parseGpApiDateTime(doc.getString("time_created")), action.getTimeCreated());
        assertEquals(doc.getString("resource"), action.getResource());
        assertEquals(doc.getString("version"), action.getVersion());
        assertEquals(doc.getString("resource_id"), action.getResourceId());
        assertEquals(doc.getString("resource_status"), action.getResourceStatus());
        assertEquals(doc.getString("http_response_code"), action.getHttpResponseCode());
        assertEquals(doc.getString("response_code"), action.getResponseCode());
        assertEquals(doc.getString("app_id"), action.getAppId());
        assertEquals(doc.getString("app_name"), action.getAppName());
        assertEquals(doc.getString("account_id"), action.getAccountId());
        assertEquals(doc.getString("account_name"), action.getAccountName());
        assertEquals(doc.getString("merchant_name"), action.getMerchantName());
    }

    @Test
    public void MapResponseTest_CreateTransaction() throws GatewayException {
        // Arrange
        String rawJson = "{\"id\":\"TRN_BHZ1whvNJnMvB6dPwf3znwWTsPjCn0\",\"time_created\":\"2020-12-04T12:46:05.235Z\",\"type\":\"SALE\",\"status\":\"PREAUTHORIZED\",\"channel\":\"CNP\",\"capture_mode\":\"LATER\",\"amount\":\"1400\",\"currency\":\"USD\",\"country\":\"US\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"TRA_6716058969854a48b33347043ff8225f\",\"account_name\":\"Transaction_Processing\",\"reference\":\"15fbcdd9-8626-4e29-aae8-050f823f995f\",\"payment_method\":{\"id\":\"PMT_9a8f1b66-58e3-409d-86df-ed5fb14ad2f6\",\"result\":\"00\",\"message\":\"[ test system ] AUTHORISED\",\"entry_mode\":\"ECOM\",\"card\":{\"brand\":\"VISA\",\"masked_number_last4\":\"XXXXXXXXXXXX5262\",\"authcode\":\"12345\",\"brand_reference\":\"PSkAnccWLNMTcRmm\",\"brand_time_created\":\"\",\"cvv_result\":\"MATCHED\"}},\"batch_id\":\"\",\"action\":{\"id\":\"ACT_BHZ1whvNJnMvB6dPwf3znwWTsPjCn0\",\"type\":\"PREAUTHORIZE\",\"time_created\":\"2020-12-04T12:46:05.235Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"Uyq6PzRbkorv2D4RQGlldEtunEeGNZll\",\"app_name\":\"sample_app_CERT\"}}";

        // Act
        Transaction transaction = GpApiMapping.mapResponse(rawJson);

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Assert
        assertEquals(doc.getString("id"), transaction.getTransactionId());
        assertEquals(doc.getAmount("amount"), transaction.getBalanceAmount());
        assertEquals(doc.getString("time_created"), transaction.getTimestamp());
        assertEquals(doc.getString("status"), transaction.getResponseMessage());
        assertEquals(doc.getString("reference"), transaction.getReferenceNumber());
        assertEquals(doc.getString("batch_id"), transaction.getBatchSummary().getBatchReference());
        assertEquals(doc.get("action").getString("result_code"), transaction.getResponseCode());
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
        assertEquals(StringUtils.toAmount(doc.getString("amount")), transaction.getBalanceAmount());
        assertEquals(doc.getString("time_created"), transaction.getTimestamp());
        assertEquals(doc.getString("status"), transaction.getResponseMessage());
        assertEquals(doc.getString("reference"), transaction.getReferenceNumber());
        assertEquals(doc.getString("batch_id"), transaction.getBatchSummary().getBatchReference());
        assertEquals(doc.get("action").getString("result_code"), transaction.getResponseCode());

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

}
