package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.*;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import org.joda.time.DateTime;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiFraudManagementTest extends BaseGpApiTest {

    private final CreditCardData card;
    private final Address address;
    private final String currency = "USD";

    public GpApiFraudManagementTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
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

        HashMap<FraudFilterMode, String> fraudFilters = new HashMap<FraudFilterMode, String>() {{
            put(FraudFilterMode.Active, FraudFilterResult.PASS.getValue());
            put(FraudFilterMode.Passive, FraudFilterResult.PASS.getValue());
            put(FraudFilterMode.Off, "");
        }};

        for (Map.Entry<FraudFilterMode, String> items : fraudFilters.entrySet()) {
            Transaction response =
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
        final String rule1 = "0c93a6c9-7649-4822-b5ea-1efa356337fd";
        final String rule2 = "a539d51a-abc1-4fff-a38e-b34e00ad0cc3";

        FraudRuleCollection rules = new FraudRuleCollection();
        rules.addRule(rule1, FraudFilterMode.Active);
        rules.addRule(rule2, FraudFilterMode.Off);

        Transaction response =
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

        for (FraudRule fraudResponseRule : response.getFraudFilterResponse().getFraudResponseRules()) {
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
        ArrayList<String> ruleList = new ArrayList<String>() {{
            add("0c93a6c9-7649-4822-b5ea-1efa356337fd");
            add("a539d51a-abc1-4fff-a38e-b34e00ad0cc3");
            add("d023a19e-6985-4fda-bb9b-5d4e0dedbb1e");
        }};

        FraudRuleCollection rules = new FraudRuleCollection();
        for (String rule : ruleList) {
            rules.addRule(rule, FraudFilterMode.Active);
        }

        Transaction response =
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

        for (FraudRule fraudResponseRule : response.getFraudFilterResponse().getFraudResponseRules()) {
            assertTrue(ruleList.contains(fraudResponseRule.getKey()));
        }
    }

    @Test
    public void FraudManagementDataSubmissionWith_AllRulesOff() throws ApiException {
        ArrayList<String> ruleList = new ArrayList<String>() {{
            add("0c93a6c9-7649-4822-b5ea-1efa356337fd");
            add("a539d51a-abc1-4fff-a38e-b34e00ad0cc3");
            add("d023a19e-6985-4fda-bb9b-5d4e0dedbb1e");
        }};

        FraudRuleCollection rules = new FraudRuleCollection();
        for (String rule : ruleList) {
            rules.addRule(rule, FraudFilterMode.Off);
        }

        Transaction response =
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

        for (FraudRule fraudResponseRule : response.getFraudFilterResponse().getFraudResponseRules()) {
            assertTrue(ruleList.contains(fraudResponseRule.getKey()));
            assertEquals(FraudFilterResult.NOT_EXECUTED.toString().toUpperCase(), fraudResponseRule.getResult());
            assertEquals(FraudFilterMode.Off, fraudResponseRule.getMode());
        }
    }

    @Test
    public void ReleaseTransactionAfterFraudResultHold() throws ApiException {
        card.setCardHolderName("Lenny Bruce");
        Transaction trn =
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
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        Transaction trn2 =
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
        Transaction trn =
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
        Transaction trn =
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
        card.setCardHolderName("Lenny Bruce");
        Transaction trn =
                card
                        .authorize(10.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        boolean errorFound = false;
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
        card.setCardHolderName("Lenny Bruce");
        Transaction trn =
                card
                        .charge(10.10)
                        .withCurrency(currency)
                        .withAddress(address)
                        .withFraudFilter(FraudFilterMode.Active)
                        .execute();

        assertNotNull(trn);
        assertEquals("SUCCESS", trn.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), trn.getResponseMessage());
        assertNotNull(trn.getFraudFilterResponse());
        assertEquals(FraudFilterMode.Active.getValue(), trn.getFraudFilterResponse().getFraudResponseMode());
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        boolean errorFound = false;
        try {
            trn
                    .refund()
                    .withCurrency(currency)
                    .execute();
        } catch (GatewayException e) {
            errorFound = true;
            assertEquals("Status Code: 400 - You can't refund a delayed transaction that has not been sent for settlement You are refunding money to a customer that has not been and never will be charged! ", e.getMessage());
            assertEquals("40087", e.getResponseText());
        } finally {
            assertTrue(errorFound);
        }
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_Charge() throws ApiException {
        Transaction trn =
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
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction trn =
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

        boolean exceptionCaught = false;
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
        Transaction trn =
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

        Transaction refundResponse = trn
                .refund()
                .withCurrency(currency)
                .execute();

        assertNotNull(refundResponse);
        assertEquals("SUCCESS", refundResponse.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue().toUpperCase(), refundResponse.getResponseMessage());
    }

    @Test
    public void FraudManagementDataSubmissionFullCycle_ChargePassive() throws ApiException {
        Transaction trn =
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
        Transaction trn =
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

        boolean errorFound = false;
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
        Transaction trn =
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

        boolean errorFound = false;
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

            Transaction trn =
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
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction trn =
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

        boolean exceptionCaught = false;
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
        Transaction trn = new Transaction();
        trn.setTransactionId(UUID.randomUUID().toString());

        boolean errorFound = false;
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
        Transaction trn =
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

        boolean errorFound = false;
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
        card.setCardHolderName("Lenny Bruce");
        Transaction trn =
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
        assertEquals(FraudFilterResult.HOLD.getValue(), trn.getFraudFilterResponse().getFraudResponseResult());

        boolean errorFound = false;
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

            Transaction trn =
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
        String idempotencyKey = UUID.randomUUID().toString();

        Transaction trn =
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

        boolean exceptionCaught = false;
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
        Transaction trn = new Transaction();
        trn.setTransactionId(UUID.randomUUID().toString());

        boolean errorFound = false;
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
        Transaction trn =
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

        boolean errorFound = false;
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
        DateTime startDate = DateTime.now().plusDays(-30);
        DateTime endDate = DateTime.now().plusDays(-3);

        TransactionSummaryPaged response =
                ReportingService
                        .findTransactionsPaged(1, 10)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Ascending)
                        .where(SearchCriteria.StartDate, startDate.toDate())
                        .and(SearchCriteria.EndDate, endDate.toDate())
                        .and(SearchCriteria.RiskAssessmentResult, FraudFilterResult.PASS)
                        .execute();

        assertTrue(!response.getResults().isEmpty());

        TransactionSummary trnSummary = response.getResults().get(0);
        assertNotNull(trnSummary.getFraudManagementResponse());
        assertEquals(FraudFilterResult.PASS.getValue(), trnSummary.getFraudManagementResponse().getFraudResponseResult());
    }

}