package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.gpApi.entities.AccessTokenInfo;
import com.global.api.logging.RequestConsoleLogger;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.Installment;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GpInstallmentTest extends BaseGpApiTest {

    private Installment new_installment;

    public GpInstallmentTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardNotPresent);
        config.setAppId("bcTDtE6wV2iCfWPqXv0FMpU86YDqvTnc");
        config.setAppKey("jdf2vlLCA13A3Fsz");
        ServicesContainer.configureService(config);
        config.setServiceUrl("https://apis-sit.globalpay.com/ucp");
        config.setCountry("MX");
        config.setEnableLogging(true);
        config.setRequestLogger(new RequestConsoleLogger());
        AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
        accessTokenInfo.setTransactionProcessingAccountName("IPP_Processing");
        config.setAccessTokenInfo(accessTokenInfo);

        ServicesContainer.configureService(config);
        getInstallment();
    }

    @Test
    public void CreateInstallment_WithMasterCardAndValidProgramSIP() throws ApiException {
        CreditCardData card = getMasterCardData();
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");
        Installment installment = new_installment.create();
        assertNotNull(installment);
        assertEquals(new_installment.getProgram(), installment.getProgram());
        assertEquals("APPROVAL", installment.getMessage());
        assertNotNull(installment.getAuthCode());
        assertEquals("00", installment.getResult());
        assertEquals("SUCCESS", installment.getAction().getResultCode());
    }


    @Test
    public void CreateInstallment_WithMasterCardAndValidProgramMIPP() throws ApiException {
        CreditCardData card = getMasterCardData();
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");
        new_installment.setProgram("mIPP");

        Installment installment = new_installment.create();

        assertNotNull(installment);
        assertEquals(new_installment.getProgram(), installment.getProgram());
        assertEquals("APPROVAL", installment.getMessage());
        assertNotNull(installment.getAuthCode());
        assertEquals("00", installment.getResult());
        assertEquals("SUCCESS", installment.getAction().getResultCode());

    }

    @Test
    public void CreateInstallment_WithVisaCardAndValidProgramSIP() throws ApiException {
        CreditCardData card = getVisaCardData();
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");

        Installment installment = new_installment.create();

        assertNotNull(installment);
        assertEquals(new_installment.getProgram(), installment.getProgram());
        assertEquals("APPROVAL", installment.getMessage());
        assertNotNull(installment.getAuthCode());
        assertEquals("00", installment.getResult());
        assertEquals("SUCCESS", installment.getAction().getResultCode());

    }

    @Test
    public void CreateInstallment_WithVisaCardAndValidProgramMIIP() throws ApiException {
        CreditCardData card = getMasterCardData();
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");
        new_installment.setProgram("mIPP");

        Installment installment = new_installment.create();

        assertNotNull(installment);
        assertEquals(new_installment.getProgram(), installment.getProgram());
        assertEquals("APPROVAL", installment.getMessage());
        assertNotNull(installment.getAuthCode());
        assertEquals("00", installment.getResult());
        assertEquals("SUCCESS", installment.getAction().getResultCode());

    }

    @Test
    public void CreateInstallment_WithExpiredVisaCardAndValidProgramMIPP() throws ApiException {
        CreditCardData card = getMasterCardData();
        card.setExpYear(2022);
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");
        new_installment.setProgram("mIPP");

        Installment installment = new_installment.create();

        assertNotNull(installment);
        assertEquals("54", installment.getResult());
        assertEquals("EXPIRED CARD", installment.getMessage());
        assertEquals("DECLINED", installment.getAction().getResultCode());

    }

    @Test
    public void CreateInstallment_WithExpiredMasterCardAndValidProgramMIPP() throws ApiException {
        CreditCardData card = getMasterCardData();
        card.setExpYear(2022);
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");
        new_installment.setProgram("mIPP");

        Installment installment = new_installment.create();

        assertNotNull(installment);
        assertEquals("54", installment.getResult());
        assertEquals("EXPIRED CARD", installment.getMessage());
        assertEquals("DECLINED", installment.getAction().getResultCode());

    }

    @Test
    public void CreateInstallment_WithVisaAndInvalidProgram() throws ApiException {
        CreditCardData card = getVisaCardData();
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");
        new_installment.setProgram("IncorrectProgram");
        try {
            Installment installment = new_installment.create();
            assertNotNull(installment);
        } catch (GatewayException exc) {
            // check for already created
            assertEquals(exc.getResponseCode(), "INVALID_REQUEST_DATA");
            assertEquals(exc.getResponseText(), "40213");
            assertEquals(exc.getMessage(), "Status Code: 400 - program contains unexpected data");
        }
    }

    @Test
    public void CreateInstallment_WithMasterAndInvalidProgram() throws ApiException {
        CreditCardData card = getMasterCardData();
        new_installment.setCreditCardData(card);
        new_installment.setEntryMode("ECOM");
        new_installment.setProgram("IncorrectProgram");
        try {
            Installment installment = new_installment.create();
            assertNotNull(installment);
        } catch (GatewayException exc) {
            // check for already created
            assertEquals(exc.getResponseCode(), "INVALID_REQUEST_DATA");
            assertEquals(exc.getResponseText(), "40213");
            assertEquals(exc.getMessage(), "Status Code: 400 - program contains unexpected data");
        }
    }

    private void getInstallment() {
        new_installment = new Installment();
        new_installment.setChannel("CNP");
        new_installment.setAmount(BigDecimal.valueOf(11099));
        new_installment.setCountry("MX");
        new_installment.setCurrency("MXN");
        new_installment.setProgram("SIP");
        new_installment.setAccountName("IPP_Processing");
        new_installment.setReference("TRANS-2019121320901");
    }

    private static CreditCardData getMasterCardData() {
        CreditCardData card = new CreditCardData();
        card.setNumber("5546259023665054");
        card.setExpMonth(05);
        card.setExpYear(2025);
        card.setCvn("123");
        card.setCardPresent(false);
        card.setReaderPresent(false);
        return card;
    }

    private CreditCardData getVisaCardData() {
        CreditCardData card = new CreditCardData();
        card.setNumber("4213168058314147");
        card.setExpMonth(07);
        card.setExpYear(2027);
        card.setCvn("123");
        card.setCardPresent(false);
        card.setReaderPresent(false);
        return card;
    }
}
