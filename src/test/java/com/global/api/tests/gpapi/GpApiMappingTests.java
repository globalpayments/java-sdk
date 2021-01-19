package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.DepositSummary;
import com.global.api.entities.reporting.DisputeSummary;
import com.global.api.gateways.GpApiConnector;
import com.global.api.mapping.GpApiMapping;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.utils.JsonDoc;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.global.api.gateways.GpApiConnector.*;
import static com.global.api.gateways.GpApiConnector.parseGpApiDateTime;
import static org.junit.Assert.*;

public class GpApiMappingTests {

    public GpApiMappingTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId("OWTP5ptQZKGj7EnvPt3uqO844XDBt8Oj")
                .setAppKey("qM31FmlFiyXRHGYh");

//      With these Nacho credentials
//        config
//                .setAppId("Uyq6PzRbkorv2D4RQGlldEtunEeGNZll")
//                .setAppKey("QDsW1ETQKHX6Y4TA");

        ServicesContainer.configureService(config, "GpApiConfig");
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
        assertEquals(doc.getDecimal("amount"), transaction.getAmount());
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
                assertEquals(card.getString("arn"), transaction.getAquirerReferenceNumber());
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

        paymentMethod.set("message", "SUCCESS");
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
        assertEquals(obj.getDecimal("amount"), transaction.getAmount());
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
        assertEquals(card.getString("arn"), transaction.getAquirerReferenceNumber());
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
        assertEquals(doc.getDecimal("amount"), deposit.getAmount());
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
            assertEquals(sales.getDecimal("amount"), deposit.getSalesTotalAmount());
        }

        if (doc.has("refunds")) {
            JsonDoc refunds = doc.get("refunds");
            assertEquals((int) refunds.getInt("count"), deposit.getRefundsTotalCount());
            assertEquals(refunds.getDecimal("amount"), deposit.getRefundsTotalAmount());
        }

        if (doc.has("disputes")) {
            JsonDoc disputes = doc.get("disputes");
            if (disputes.has("chargebacks")) {
                JsonDoc chargebacks = disputes.get("chargebacks");
                assertEquals((int) chargebacks.getInt("count"), deposit.getChargebackTotalCount());
                assertEquals(chargebacks.getDecimal("amount"), deposit.getChargebackTotalAmount());
            }

            if (disputes.has("reversals")) {
                JsonDoc reversals = disputes.get("reversals");
                assertEquals((int) reversals.getInt("count"), deposit.getAdjustmentTotalCount());
                assertEquals(reversals.getDecimal("amount"), deposit.getAdjustmentTotalAmount());
            }
        }
        if (doc.has("fees")) {
            assertEquals(doc.get("fees").getDecimal("amount"), deposit.getFeesTotalAmount());
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
        assertEquals(parseGpApiDate(doc.getString("time_created")), dispute.getCaseIdTime());
        assertEquals(doc.getString("status"), dispute.getCaseStatus());
        assertEquals(doc.getString("stage"), dispute.getCaseStage());
        assertEquals(doc.getDecimal("amount"), dispute.getCaseAmount());
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
        assertEquals(doc.getDate("time_to_respond_by"), dispute.getRespondByDate());
        assertEquals(doc.getString("result"), dispute.getResult());
        assertEquals(doc.getDecimal("last_adjustment_amount"), dispute.getLastAdjustmentAmount());
        assertEquals(doc.getString("last_adjustment_currency"), dispute.getLastAdjustmentCurrency());
        assertEquals(doc.getString("last_adjustment_funding"), dispute.getLastAdjustmentFunding());
    }

    @Test
    public void MapResponseTest() {
        // Arrange
        String rawJson = "{\"id\":\"TRN_BHZ1whvNJnMvB6dPwf3znwWTsPjCn0\",\"time_created\":\"2020-12-04T12:46:05.235Z\",\"type\":\"SALE\",\"status\":\"PREAUTHORIZED\",\"channel\":\"CNP\",\"capture_mode\":\"LATER\",\"amount\":\"1400\",\"currency\":\"USD\",\"country\":\"US\",\"merchant_id\":\"MER_c4c0df11039c48a9b63701adeaa296c3\",\"merchant_name\":\"Sandbox_merchant_2\",\"account_id\":\"TRA_6716058969854a48b33347043ff8225f\",\"account_name\":\"Transaction_Processing\",\"reference\":\"15fbcdd9-8626-4e29-aae8-050f823f995f\",\"payment_method\":{\"result\":\"00\",\"message\":\"[ test system ] AUTHORISED\",\"entry_mode\":\"ECOM\",\"card\":{\"brand\":\"VISA\",\"masked_number_last4\":\"XXXXXXXXXXXX5262\",\"authcode\":\"12345\",\"brand_reference\":\"PSkAnccWLNMTcRmm\",\"brand_time_created\":\"\",\"cvv_result\":\"MATCHED\"}},\"batch_id\":\"\",\"action\":{\"id\":\"ACT_BHZ1whvNJnMvB6dPwf3znwWTsPjCn0\",\"type\":\"PREAUTHORIZE\",\"time_created\":\"2020-12-04T12:46:05.235Z\",\"result_code\":\"SUCCESS\",\"app_id\":\"Uyq6PzRbkorv2D4RQGlldEtunEeGNZll\",\"app_name\":\"sample_app_CERT\"}}";

        // Act
        Transaction transaction = GpApiMapping.mapResponse(rawJson);

        JsonDoc doc = JsonDoc.parse(rawJson);

        // Assert
        assertEquals(doc.getString("id"), transaction.getTransactionId());
        assertEquals(doc.getDecimal("amount"), transaction.getBalanceAmount());
        assertEquals(doc.getString("time_created"), transaction.getTimestamp());
        assertEquals(doc.getString("status"), transaction.getResponseMessage());
        assertEquals(doc.getString("reference"), transaction.getReferenceNumber());
        assertEquals(doc.getString("batch_id"), transaction.getBatchSummary().getSequenceNumber());
        assertEquals(doc.get("action").getString("result_code"), transaction.getResponseCode());
        assertEquals(doc.getString("id"), transaction.getToken());

        if (doc.has("payment_method")) {
            JsonDoc paymentMethod = doc.get("payment_method");
            assertEquals(paymentMethod.getString("result"), transaction.getAuthorizationCode());
            if (paymentMethod.has("card")) {
                JsonDoc card = paymentMethod.get("card");
                assertEquals(card.getString("brand"), transaction.getCardType());
                assertEquals(card.getString("masked_number_last4"), transaction.getCardLast4());
            }
        }

        if (doc.has("card")) {
            JsonDoc card = doc.get("card");
            assertEquals(card.getString("number"), transaction.getCardNumber());
            assertEquals(card.getString("brand"), transaction.getCardType());
            assertEquals((int) card.getInt("expiry_month"), transaction.getCardExpMonth());
            assertEquals((int) card.getInt("expiry_year"), transaction.getCardExpYear());
        }
    }

}