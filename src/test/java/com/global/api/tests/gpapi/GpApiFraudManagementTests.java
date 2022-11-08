package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.FraudRuleCollection;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.Assert.*;

public class GpApiFraudManagementTests extends BaseGpApiTest {

    private final CreditCardData card;
    private final Address address;
    private final String currency = "USD";

    public GpApiFraudManagementTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID_FRAUD)
                .setAppKey(APP_KEY_FRAUD)
                .setChannel(Channel.CardNotPresent.getValue());

        AccessTokenInfo accessTokenInfo =
                new AccessTokenInfo()
                        .setTransactionProcessingAccountName("transaction_processing");

        config.setAccessTokenInfo(accessTokenInfo);

        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        card = new CreditCardData();
        card.setNumber("4263970000005262");
        card.setExpMonth(DateTime.now().getMonthOfYear());
        card.setExpYear(DateTime.now().getYear() + 1);
        card.setCvn("131");
        card.setCardHolderName("James Mason");

        address = new Address();
        address.setStreetAddress1("123 Main St.");
        address.setCity("Downtown");
        address.setState("NJ");
        address.setCountry("US");
        address.setPostalCode("12345");
    }

    //TODO - For filter set OFF the response fraud result is set incorrectly as Hold
    @Test
    public void FraudManagementDataSubmissions() throws ApiException {

        var fraudFilters = new HashMap<FraudFilterMode, String>() {{
            put(FraudFilterMode.Active, FraudFilterResult.PASS.getValue());
            put(FraudFilterMode.Passive, FraudFilterResult.PASS.getValue());
            put(FraudFilterMode.Off, "");
        }};

        for (var items : fraudFilters.entrySet()) {
            var response =
                    card
                            .charge(98.10)
                            .withCurrency(currency)
                            .withAddress(address)
                            .withFraudFilter(items.getKey())
                            .execute();

            assertNotNull(response);
            assertEquals("SUCCESS", response.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), response.getResponseMessage());
            assertNotNull(response.getFraudFilterResponse());
            assertEquals(items.getKey().toString().toUpperCase(), response.getFraudFilterResponse().getFraudResponseMode());
            assertEquals(items.getValue(), response.getFraudFilterResponse().getFraudResponseResult());
        }
    }

    @Test
    public void FraudManagementDataSubmissionWithRules() throws ApiException {
        final String rule1 = "2c49c2e6-5843-4275-9b92-8c9b6dc8e566";
        final String rule2 = "2cfa3a28-f8f3-42f8-abbf-79b54e35de16";

        var rules = new FraudRuleCollection();
        rules.addRule(rule1, FraudFilterMode.Active);
        rules.addRule(rule2, FraudFilterMode.Off);

        var response =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active, rules)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), response.getResponseMessage());
        assertNotNull(response.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), response.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), response.getFraudFilterResponse().getFraudResponseResult());

        for (var fraudResponseRule : response.getFraudFilterResponse().getFraudResponseRules()) {
            if (fraudResponseRule.getKey().equals(rule1)) {
                assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), fraudResponseRule.getResult());
            }
            if (fraudResponseRule.getKey().equals(rule2)) {
                assertEquals(FraudFilterResult.NOT_EXECUTED.toString().toUpperCase(), fraudResponseRule.getResult());
            }
        }
    }

    @Test
    public void FraudManagementDataSubmissionWith_AllRulesActive() throws ApiException {
        var ruleList = new ArrayList<String>() {{
            add("2c49c2e6-5843-4275-9b92-8c9b6dc8e566");
            add("2cfa3a28-f8f3-42f8-abbf-79b54e35de16");
            add("21db158b-4541-4217-aa81-927596465547");
            add("6acbcb2e-79c7-40c3-8c17-b65c5fba2a54");
            add("a7da55fb-69c4-4c41-abb6-c4dded40354e");
        }};

        var rules = new FraudRuleCollection();
        for (var rule : ruleList) {
            rules.addRule(rule, FraudFilterMode.Active);
        }

        var response =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active, rules)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), response.getResponseMessage());
        assertNotNull(response.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue().toUpperCase(), response.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), response.getFraudFilterResponse().getFraudResponseResult());

        for (var fraudResponseRule : response.getFraudFilterResponse().getFraudResponseRules()) {
            assertTrue(ruleList.contains(fraudResponseRule.getKey()));
        }
    }

    @Test
    public void FraudManagementDataSubmissionWith_AllRulesOff() throws ApiException {
        var ruleList = new ArrayList<String>() {{
            add("2c49c2e6-5843-4275-9b92-8c9b6dc8e566");
            add("2cfa3a28-f8f3-42f8-abbf-79b54e35de16");
            add("21db158b-4541-4217-aa81-927596465547");
            add("6acbcb2e-79c7-40c3-8c17-b65c5fba2a54");
            add("a7da55fb-69c4-4c41-abb6-c4dded40354e");
        }};

        var rules = new FraudRuleCollection();
        for (var rule : ruleList) {
            rules.addRule(rule, FraudFilterMode.Off);
        }

        var response =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active, rules)
                        .execute();

        assertNotNull(response);
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), response.getResponseMessage());
        assertNotNull(response.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue().toUpperCase(), response.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.NOT_EXECUTED.toString().toUpperCase(), response.getFraudFilterResponse().getFraudResponseResult());

        for (var fraudResponseRule : response.getFraudFilterResponse().getFraudResponseRules()) {
            assertTrue(ruleList.contains(fraudResponseRule.getKey()));
            assertEquals(FraudFilterResult.NOT_EXECUTED.toString().toUpperCase(), fraudResponseRule.getResult());
            assertEquals(FraudFilterMode.Off, fraudResponseRule.getMode());
        }
    }

    @Test
    public void ReleaseTransactionAfterFraudResultHold() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .withCustomerIpAddress("123.123.123.123")
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        var trn2 =
                trn
                        .release()
                        .withReasonCode(ReasonCode.FalsePositive)
                        .execute();

        assertNotNull(trn2);
        assertEquals("SUCCESS", trn2.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn2.getResponseMessage());
        assertNotNull(trn2.getFraudFilterResponse());
        assertEquals(FraudFilterResult.RELEASE_SUCCESSFUL.getValue(), trn2.getFraudFilterResponse().getFraudResponseResult());
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle() throws ApiException {
        var trn =
                card
                        .authorize(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());

        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .hold()
                        .withReasonCode(ReasonCode.Fraud)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .release()
                        .withReasonCode(ReasonCode.FalsePositive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.RELEASE_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .capture()
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), trn.getResponseMessage());
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_HoldAndReleaseWithoutReasonCode() throws ApiException {
        var trn =
                card
                        .authorize(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .hold()
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .release()
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.RELEASE_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .capture()
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
    }

    @Test
    public void CaptureTransactionAfterFraudResultHold() throws ApiException {
        var trn =
                card
                        .authorize(10.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .withCustomerIpAddress("123.123.123.123")
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .capture()
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("50020", e.getResponseText());
            assertEquals("Status Code: 400 - This transaction has been held", e.getMessage());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void RefundTransactionAfterFraudResultHold() throws ApiException {
        var trn =
                card
                        .charge(10.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .withCustomerIpAddress("123.123.123.123")
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .refund()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 502 - The refund password you entered was incorrect ", e.getMessage());
            assertEquals("50017", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_Charge() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .hold()
                        .withReasonCode(ReasonCode.Fraud)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .release()
                        .withReasonCode(ReasonCode.FalsePositive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.RELEASE_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_Charge_WithIdempotencyKey() throws ApiException {
        var idempotencyKey = UUID.randomUUID().toString();

        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        var exceptionCaught = false;
        try {
            card.charge(1)
                    .withCurrency(currency)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + trn.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_ChargeThenRefund() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .hold()
                        .withReasonCode(ReasonCode.Fraud)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .release()
                        .withReasonCode(ReasonCode.FalsePositive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.RELEASE_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .refund()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 502 - The refund password you entered was incorrect ", e.getMessage());
            assertEquals("50017", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_ChargePassive() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Passive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Passive.toString().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .hold()
                        .withReasonCode(ReasonCode.Fraud)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .release()
                        .withReasonCode(ReasonCode.FalsePositive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.RELEASE_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_Charge_ThenReleaseWithoutHold() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Passive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Passive.toString().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .release()
                    .withReasonCode(ReasonCode.FalsePositive)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - Cant release transaction that is not held", e.getMessage());
            assertEquals("50020", e.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_Authorize_ThenReleaseWithoutHold() throws ApiException {
        var trn =
                card
                        .authorize(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Passive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Passive.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .release()
                    .withReasonCode(ReasonCode.FalsePositive)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - Cant release transaction that is not held", e.getMessage());
            assertEquals("50020", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void Release_AllReasonCodes() throws ApiException {
        for (ReasonCode reasonCode : ReasonCode.values()) {
            if (reasonCode.equals(ReasonCode.Fraud) || reasonCode.equals(ReasonCode.OutOfStock)) {
                continue;
            }

            var trn =
                    card
                            .charge(98.10)
                            .withCurrency(currency)
                            .withAddress(address)
                            .withFraudFilter(FraudFilterMode.Active)
                            .execute();

            assertNotNull(trn);
            assertEquals("SUCCESS", trn.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
            assertNotNull(trn.getFraudFilterResponse());
            assertEquals(FraudFilterMode.Active.getValue(),
                    trn.getFraudFilterResponse().getFraudResponseMode());
            assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(),
                    trn.getFraudFilterResponse().getFraudResponseResult());

            trn = trn
                    .hold()
                    .withReasonCode(ReasonCode.Fraud)
                    .execute();

            assertNotNull(trn);
            assertEquals("SUCCESS", trn.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
            assertNotNull(trn.getFraudFilterResponse());
            assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

            trn =
                    trn
                            .release()
                            .withReasonCode(reasonCode)
                            .execute();

            assertNotNull(trn);
            assertEquals("SUCCESS", trn.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
            assertNotNull(trn.getFraudFilterResponse());
            assertEquals(FraudFilterResult.RELEASE_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());
        }
    }

    @Test
    public void Release_WithIdempotencyKey() throws ApiException {
        var idempotencyKey = UUID.randomUUID().toString();

        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        trn =
                trn
                        .hold()
                        .withReasonCode(ReasonCode.Fraud)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        var exceptionCaught = false;
        try {
            trn.release()
                    .withReasonCode(ReasonCode.FalsePositive)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + trn.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void Release_RandomTransaction() {
        var trn = new Transaction();
        trn.setTransactionId(UUID.randomUUID().toString());

        var errorFound = false;
        try {
            trn
                    .release()
                    .withReasonCode(ReasonCode.Other)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 404 - Transaction " + trn.getTransactionId() + " not found at this location.", e.getMessage());
            assertEquals("40008", e.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void Release_InvalidReason() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Passive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Passive.toString().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .release()
                    .withReasonCode(ReasonCode.Fraud)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - reason_code value is invalid. Please check the reason_code is entered correctly", e.getMessage());
            assertEquals("40259", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void HoldTransactionAfterFraudResultHold() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withCustomerIpAddress("123.123.123.123")
                        .withFraudFilter(FraudFilterMode.Active)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .hold()
                    .withReasonCode(ReasonCode.Fraud)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - This transaction is already held", e.getMessage());
            assertEquals("50020", e.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void Hold_AllReasonCodes() throws ApiException {
        for (ReasonCode reasonCode : ReasonCode.values()) {
            if (reasonCode.equals(ReasonCode.FalsePositive) || reasonCode.equals(ReasonCode.InStock)) {
                continue;
            }

            var trn =
                    card
                            .charge(98.10)
                            .withCurrency(currency)
                            .withAddress(address)
                            .withFraudFilter(FraudFilterMode.Active)
                            .execute();

            assertNotNull(trn);
            assertEquals("SUCCESS", trn.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
            assertNotNull(trn.getFraudFilterResponse());
            assertEquals(FraudFilterMode.Active.getValue(),
                    trn.getFraudFilterResponse().getFraudResponseMode());
            assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(),
                    trn.getFraudFilterResponse().getFraudResponseResult());

            trn =
                    trn
                            .hold()
                            .withReasonCode(reasonCode)
                            .execute();

            assertNotNull(trn);
            assertEquals("SUCCESS", trn.getResponseCode());
            assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
            assertNotNull(trn.getFraudFilterResponse());
            assertEquals(FraudFilterResult.HOLD_SUCCESSFUL.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());
        }
    }

    @Test
    public void Hold_WithIdempotencyKey() throws ApiException {
        var idempotencyKey = UUID.randomUUID().toString();

        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .withIdempotencyKey(idempotencyKey)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        var exceptionCaught = false;
        try {
            trn
                    .hold()
                    .withReasonCode(ReasonCode.Fraud)
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + trn.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void Hold_RandomTransaction() {
        var trn = new Transaction();
        trn.setTransactionId(UUID.randomUUID().toString());

        var errorFound = false;
        try {
            trn
                    .hold()
                    .withReasonCode(ReasonCode.Other)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 404 - Transaction " + trn.getTransactionId() + " not found at this location.", e.getMessage());
            assertEquals("40008", e.getResponseText());
        } catch (ApiException e) {
            throw new RuntimeException(e);
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void Hold_InvalidReason() throws ApiException {
        var trn =
                card
                        .charge(98.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Passive)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Passive.toString().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.PASS.getValue().toUpperCase(), trn.getFraudFilterResponse().getFraudResponseResult());

        var errorFound = false;
        try {
            trn
                    .hold()
                    .withReasonCode(ReasonCode.FalsePositive)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - reason_code value is invalid. Please check the reason_code is entered correctly", e.getMessage());
            assertEquals("40259", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void GetTransactionWithFraudCheck() throws ApiException {
        var startDate = DateTime.now().plusDays(-30);
        var endDate = DateTime.now().plusDays(-3);

        var response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate.toDate())
                        .and(SearchCriteria.EndDate, endDate.toDate())
                        .and(SearchCriteria.RiskAssessmentResult, FraudFilterResult.HOLD)
                        .execute();

        assertTrue(response.getResults().size() > 0);

        var trnSummary = response.getResults().get(0);
        assertNotNull(trnSummary.getFraudManagementResponse());
        assertEquals(FraudFilterResult.HOLD.getValue(), trnSummary.getFraudManagementResponse().getFraudResponseResult());
    }

}