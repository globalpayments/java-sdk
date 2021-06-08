package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GpApiDebitTests extends BaseGpApiTest {

    private final DebitTrackData track = new DebitTrackData();

    public GpApiDebitTests() throws ApiException {

        GpApiConfig config = new GpApiConfig();

        // GP-API settings
        config
                .setAppId(APP_ID)
                .setAppKey(APP_KEY)
                .setChannel(Channel.CardPresent.getValue());

        config.setEnableLogging(true);

        ServicesContainer.configureService(config, GP_API_CONFIG_NAME);
    }

    @Test
    public void debitSaleSwipe() throws ApiException {
        track.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        track.setPinBlock("32539F50C245A6A93D123412324000AA");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response =
                track
                        .charge(new BigDecimal("17.01"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void debitRefundSwipe() throws ApiException {
        track.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        track.setEntryMethod(EntryMethod.Swipe);

        Transaction response =
                track
                        .refund(new BigDecimal("12.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void debitRefundChip() throws ApiException {
        track.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        track.setEntryMethod(EntryMethod.Swipe);

        String tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390105FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";

        Transaction response =
                track
                        .refund(new BigDecimal("12.99"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withTagData(tagData)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void debitSaleSwipeEncrypted() throws ApiException {
        track.setValue("&lt;E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|&gt;");
        track.setPinBlock("32539F50C245A6A93D123412324000AA");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setEncryptionData(EncryptionData.version1());

        Transaction response =
                track
                        .charge(new BigDecimal("17.01"))
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void debitSaleSwipeChip() throws ApiException {
        track.setValue(";4024720012345671=18125025432198712345?");
        track.setEntryMethod(EntryMethod.Swipe);
        track.setPinBlock("AFEC374574FC90623D010000116001EE");

        String tagData = "82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019";

        Transaction response = track.charge(new BigDecimal("15.99"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withTagData(tagData)
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    @Test
    public void debitSaleContactlessChip() throws ApiException {
        track.setValue(";4024720012345671=18125025432198712345?");
        track.setEntryMethod(EntryMethod.Proximity);
        track.setPinBlock("AFEC374574FC90623D010000116001EE");

        String tagData = "82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019";

        Transaction response = track.charge(new BigDecimal("25.95"))
                .withCurrency("USD")
                .withAllowDuplicates(true)
                .withTagData(tagData)
                .execute(GP_API_CONFIG_NAME);

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), response.getResponseMessage());
    }

    // .NET CreditCardReauthorizeTransaction() test is into GpApiCreditTests.java class

    @After
    public void generalValidations() {
        assertEquals("Visa", track.getCardType());
    }

}