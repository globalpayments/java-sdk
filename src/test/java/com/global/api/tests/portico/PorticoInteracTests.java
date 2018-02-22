package com.global.api.tests.portico;

import com.global.api.ServicesConfig;
import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.AccountType;
import com.global.api.entities.enums.EmvChipCondition;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.DebitTrackData;
import org.junit.Test;

import java.math.BigDecimal;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PorticoInteracTests {
    private DebitTrackData track;
    private String tagData;

    public PorticoInteracTests() throws ApiException {
        ServicesConfig config = new ServicesConfig();
        config.setLicenseId(124964);
        config.setSiteId(124992);
        config.setDeviceId(145);
        config.setUsername("9158402");
        config.setPassword("$Test1234");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configure(config);

        track = new DebitTrackData();
        track.setValue(";4024720012345671=18125025432198712345?");
        track.setEntryMethod(EntryMethod.Proximity);
        track.setPinBlock("AFEC374574FC90623D010000116001EE");

        tagData = "82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019";
    }

    @Test
    public void debitInteracPosNumber() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withPosSequenceNumber("000010010770")
                .withTagData(tagData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracAccountTypeChecking() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAccountType(AccountType.Checking)
                .withTagData(tagData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracAccountTypeSavings() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAccountType(AccountType.Savings)
                .withTagData(tagData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracMessageAuthenticationCode() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withMessageAuthenticationCode("AFEC374")
                .withTagData(tagData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracChipConditionFailed() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitInteracChipConditionFailedTwice() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test(expected = BuilderException.class)
    public void debitInteracChipConditionWithTagData() throws ApiException {
        track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withChipCondition(EmvChipCondition.ChipFailPreviousSuccess)
                .withTagData(tagData)
                .execute();
    }

    @Test
    public void debitInteracResponseFields() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withPosSequenceNumber("1")
                .withAccountType(AccountType.Checking)
                .withMessageAuthenticationCode("AFEC374")
                .withTagData(tagData)
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
        assertNotNull(response.getEmvIssuerResponse());
        assertNotNull(response.getDebitMac());
        assertNotNull(response.getHostResponseDate());
    }
}
