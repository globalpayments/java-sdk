package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.BuilderException;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.PorticoConfig;
import org.junit.Test;

import java.math.BigDecimal;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PorticoInteracTests {
    private DebitTrackData track;
    private String tagData;

    public PorticoInteracTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setLicenseId(374209);
        config.setSiteId(374391);
        config.setDeviceId(5246);
        config.setUsername("gateway1082907");
        config.setPassword("$Test1234");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");
        config.setEnableLogging(true);

        ServicesContainer.configureService(config);

        track = new DebitTrackData();
//        track.setValue(";4024720012345671=18125025432198712345?");
//        track.setValue(";0012030000000003=2812220?");
        track.setValue(";0012030000000003=2812220016290740?");
        track.setEntryMethod(EntryMethod.Swipe);
//        track.setPinBlock("AFEC374574FC90623D010000116001EE");

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
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());
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
    public void debitInteracAccountTypeSavingsCardHolderLanguage() throws ApiException {
        Transaction response = track.charge(new BigDecimal("8"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withAccountType(AccountType.Savings)
                .withTagData(tagData)
                .withCardHolderLanguage("en-US")
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

    @Test
    public void debitInteracReversal() throws ApiException {
        DebitTrackData track2 = new DebitTrackData();
        track2.setValue(";0012020000001=2812220016290740?");

        String localTagData = "4F07a00000027710105007496e74657261635F2A0201245F34010182021c008407a0000002771010950580000090009A032011099B0268009C01009F02060000000003009F03060000000000009F0607a00000027710109F0702ab009F080200019F090200019F0D05f078fcb8009F0E050010d800009F0F05fc7824f8809F100706040a03a400009F1A0201249F1E0831323334353637389F21032229099F26080ddc6ce267ff24de9F2701809F330360b8c89F34030403029F3501229F360200799F37040a3f42e69F3901059F40057000a0a0019F410400000003";

        Transaction response = track2.charge(new BigDecimal("989.20"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withPosSequenceNumber("000010010770")
                .withTagData(localTagData)
                .execute();
        assertNotNull(response);
        assertEquals(response.getResponseMessage(), "00", response.getResponseCode());

        Transaction reversal = response.reverse(new BigDecimal("989.20"))
                .withCurrency("USD")
                .withTagData(localTagData)
                .execute();
        assertNotNull(reversal);
        assertEquals(reversal.getResponseMessage(), "00", reversal.getResponseCode());
    }

    @Test
    public void debitAuth() throws ApiException {
        Transaction response =
                track
                        .authorize(new BigDecimal("100"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withCardHolderLanguage("ENGLISH")
                        .withPosSequenceNumber("000010010180")
                        .withTagData(tagData)
                        .withAccountType(AccountType.Savings)
                        .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitAddToBatch() throws ApiException {
        Transaction response =
                track
                        .authorize(new BigDecimal("14.01"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withCardHolderLanguage("ENGLISH")
                        .withPosSequenceNumber("000010010180")
                        .withTagData(tagData)
                        .withAccountType(AccountType.Savings)
                        .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction transaction =
                Transaction.fromId(
                    response.getTransactionId(),
                    PaymentMethodType.Debit,
                    track
                );

        Transaction capture =
                transaction
                        .capture(new BigDecimal(16))
                        .withCurrency("USD")
                        .withPosSequenceNumber("000010010180")
                        .withTagData(tagData)
                        .withAccountType(AccountType.Savings)
                        .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
                        .execute();
        assertNotNull(capture);

        assertEquals("00", capture.getResponseCode());
    }

    @Test
    public void debitAuthChipConditionFailedTwice() throws ApiException {
        Transaction response =
                track
                        .authorize(new BigDecimal("10"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withCardHolderLanguage("ENGLISH")
                        .withPosSequenceNumber("000010010180")
                        .withTagData(tagData)
                        .withAccountType(AccountType.Savings)
                        .withChipCondition(EmvChipCondition.ChipFailPreviousFail)
                        .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }


    @Test
    public void debitAuthAccountTypeSavings() throws ApiException {
        Transaction response =
                track
                        .authorize(new BigDecimal("10"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withCardHolderLanguage("ENGLISH")
                        .withPosSequenceNumber("000010010180")
                        .withTagData(tagData)
                        .withAccountType(AccountType.Savings)
                        .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitAuthAccountTypeChecking() throws ApiException {
        Transaction response =
                track
                        .authorize(new BigDecimal("10"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withCardHolderLanguage("ENGLISH")
                        .withPosSequenceNumber("000010010180")
                        .withTagData(tagData)
                        .withAccountType(AccountType.Checking)
                        .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void debitReversalWithTransactionIDAndTrackData() throws ApiException {

        // Preparing reversal transaction form the ID.
        Transaction reversal = Transaction.fromId(
                "1619977752",
                PaymentMethodType.Debit,
                track);

        // Performing the reversal for the generated transaction.
        Transaction response = reversal
                .reverse(new BigDecimal("12.21"))
                .withCurrency("USD")
                .withTagData(tagData)
                .withPosSequenceNumber("000010010130")
                .withReversalReasonCode(ReversalReasonCode.ChipCardDecline)
                .withInvoiceNumber("000027")
                .execute();

        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }
}
