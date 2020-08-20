package com.global.api.tests.network.vaps;

import com.global.api.ServicesContainer;
import com.global.api.entities.Address;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.network.entities.NtsData;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.network.enums.*;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.AcceptorConfig;
import com.global.api.serviceConfigs.NetworkGatewayConfig;
import com.global.api.tests.BatchProvider;
import com.global.api.tests.StanGenerator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VapsPartialApprovalTests {
    public VapsPartialApprovalTests() throws ApiException {
        Address address = new Address();
        address.setName("My STORE");
        address.setStreetAddress1("1 MY STREET");
        address.setCity("MYTOWN");
        address.setPostalCode("90210");
        address.setState("KY");
        address.setCountry("USA");

        AcceptorConfig acceptorConfig = new AcceptorConfig();
        acceptorConfig.setAddress(address);

        // data code values
        acceptorConfig.setCardDataInputCapability(CardDataInputCapability.ContactlessEmv_ContactEmv_ContactlessMsd_MagStripe_KeyEntry);
        acceptorConfig.setCardHolderAuthenticationCapability(CardHolderAuthenticationCapability.PIN);
        acceptorConfig.setCardHolderAuthenticationEntity(CardHolderAuthenticationEntity.ByMerchant);
        acceptorConfig.setTerminalOutputCapability(TerminalOutputCapability.Printing_Display);

        // hardware software config values
        acceptorConfig.setHardwareLevel("34");
        acceptorConfig.setSoftwareLevel("21205710");

        // pos configuration values
        acceptorConfig.setSupportsPartialApproval(true);
        acceptorConfig.setSupportsShutOffAmount(true);
        acceptorConfig.setSupportsReturnBalance(true);
        acceptorConfig.setSupportsDiscoverNetworkReferenceId(true);
        acceptorConfig.setSupportsAvsCnvVoidReferrals(true);
        acceptorConfig.setSupportsEmvPin(true);

        // gateway config
        NetworkGatewayConfig config = new NetworkGatewayConfig();
        config.setPrimaryEndpoint("test.txns-c.secureexchange.net");
        config.setPrimaryPort(15031);
        config.setSecondaryEndpoint("test.txns.secureexchange.net");
        config.setSecondaryPort(15031);
        config.setCompanyId("0044");
        config.setTerminalId("0000912197711");
        config.setAcceptorConfig(acceptorConfig);
        config.setEnableLogging(true);
        config.setStanProvider(StanGenerator.getInstance());
        config.setBatchProvider(BatchProvider.getInstance());

        ServicesContainer.configureService(config);

        // with merchant type
        config.setMerchantType("5542");
        ServicesContainer.configureService(config, "ICR");
    }

    @Test
    public void test_000_credit_partial_approval() throws ApiException {
        CreditCardData card = new CreditCardData();
        card.setNumber("4427802641004797");
        card.setExpMonth(12);
        card.setExpYear(2025);
        card.setCvn("123");

        Transaction response = card.authorize(new BigDecimal("1"), true)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(response);
        assertEquals("002", response.getResponseCode());
        assertNotNull(response.getAuthorizedAmount());

        BigDecimal authorizedAmount = response.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("1"), authorizedAmount);

        Transaction captureResponse = response.capture(authorizedAmount)
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(captureResponse);

        PriorMessageInformation pmi = captureResponse.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("201", pmi.getFunctionCode());
        assertEquals("000", captureResponse.getResponseCode());
    }

    @Test
    public void test_001_debit_partial_approval_cancel() throws ApiException {
        DebitTrackData track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");
        track.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));

        Transaction sale = track.charge(new BigDecimal("110"))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        BigDecimal approvedAmount = sale.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("110"), approvedAmount);

        Transaction cancel = sale.cancel(approvedAmount)
                .execute("ICR");
        assertNotNull(cancel);

        PriorMessageInformation pmi = cancel.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("4353", pmi.getMessageReasonCode());
        assertEquals("400", cancel.getResponseCode());
    }

    @Test
    public void test_002_transaction_recreation() throws ApiException {
        DebitTrackData track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");
        track.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));

        Transaction sale = track.charge(new BigDecimal("110"))
                .withCurrency("USD")
                .execute("ICR");
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        BigDecimal approvedAmount = sale.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("110"), approvedAmount);

        Transaction trans = Transaction.fromBuilder()
                .withAmount(new BigDecimal("110"))
                .withAuthorizedAmount(approvedAmount)
                .withPartialApproval(true)
                .withAuthorizationCode(sale.getAuthorizationCode())
                .withPaymentMethod(track)
                .withSystemTraceAuditNumber(sale.getSystemTraceAuditNumber())
                .withProcessingCode(sale.getProcessingCode())
                .withMessageTypeIndicator(sale.getMessageTypeIndicator())
                .withTransactionTime(sale.getOriginalTransactionTime())
                .withAcquirerId(sale.getAcquiringInstitutionId())
                .build();

        Transaction cancel = trans.cancel(approvedAmount)
                .execute("ICR");
        assertNotNull(cancel);

        PriorMessageInformation pmi = cancel.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("4353", pmi.getMessageReasonCode());
        assertEquals("400", cancel.getResponseCode());
    }

    @Test
    public void test_003_transaction_recreation_no_flag() throws ApiException {
        DebitTrackData track = new DebitTrackData();
        track.setValue("4355567063338=2012101HJNw/ewskBgnZqkL");
        track.setPinBlock("62968D2481D231E1A504010024A00014");
        track.setEncryptionData(EncryptionData.version2("/wECAQEEAoFGAgEH4gcOTDT6jRZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0m+/d4SO9TEshhRGUUQzVBrBvP/Os1qFx+6zdQp1ejjUCoDmzoUMbil9UG73zBxxTOy25f3Px0p8joyCh8PEWhADz1BkROJT3q6JnocQE49yYBHuFK0obm5kqUcYPfTY09vPOpmN+wp45gJY9PhkJF5XvPsMlcxX4/JhtCshegz4AYrcU/sFnI+nDwhy295BdOkVN1rn00jwCbRcE900kj3UsFfyc", "2"));

        Transaction sale = track.charge(new BigDecimal("110"))
                .withCurrency("USD")
                .execute();
        assertNotNull(sale);
        assertEquals("000", sale.getResponseCode());

        BigDecimal approvedAmount = sale.getAuthorizedAmount();
        assertNotEquals(new BigDecimal("110"), approvedAmount);

        Transaction trans = Transaction.fromBuilder()
                .withAmount(new BigDecimal("110"))
                .withAuthorizedAmount(approvedAmount)
                .withPartialApproval(false)
                .withAuthorizationCode(sale.getAuthorizationCode())
                .withPaymentMethod(track)
                .withSystemTraceAuditNumber(sale.getSystemTraceAuditNumber())
                .withProcessingCode(sale.getProcessingCode())
                .withMessageTypeIndicator(sale.getMessageTypeIndicator())
                .withTransactionTime(sale.getOriginalTransactionTime())
                .withAcquirerId(sale.getAcquiringInstitutionId())
                .build();

        Transaction cancel = trans.cancel(approvedAmount)
                .execute();
        assertNotNull(cancel);

        PriorMessageInformation pmi = cancel.getMessageInformation();
        assertNotNull(pmi);
        assertEquals("4352", pmi.getMessageReasonCode());
        assertEquals("400", cancel.getResponseCode());
    }
}
