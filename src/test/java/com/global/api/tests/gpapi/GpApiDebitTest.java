package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import java.math.BigDecimal;

public class GpApiDebitTest extends BaseGpApiTest {

    private final DebitTrackData debitTrackData = new DebitTrackData();
    private final BigDecimal amount = new BigDecimal("12.02");
    private final String currency = "USD";
    private final String tagData;

    public GpApiDebitTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardPresent);
        ServicesContainer.configureService(config);

        debitTrackData.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        debitTrackData.setPinBlock("32539F50C245A6A93D123412324000AA");
        debitTrackData.setEntryMethod(EntryMethod.Swipe);

        tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390105FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";
    }

    @Test
    public void DebitSaleSwipe() throws ApiException {
        Transaction response =
                debitTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void DebitSaleSwipe_Chip() throws ApiException {
        Transaction response =
                debitTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void DebitSaleSwipe_AuthorizeThenCapture() throws ApiException {
        Transaction response =
                debitTrackData
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Preauthorized);

        Transaction captureResponse =
                response
                        .capture(amount)
                        .withCurrency(currency)
                        .execute();

        assertTransactionResponse(captureResponse, TransactionStatus.Captured);
    }

    @Test
    public void DebitRefundSwipe() throws ApiException {
        Transaction response =
                debitTrackData
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void DebitRefundChip() throws ApiException {
        debitTrackData.setPinBlock(null);

        Transaction response =
                debitTrackData
                        .refund(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void DebitSaleSwipeEncrypted() throws ApiException {
        debitTrackData.setValue("&lt;E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|&gt;");
        debitTrackData.setEncryptionData(EncryptionData.version1());

        Transaction response =
                debitTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void DebitSaleSwipeChip_NewDebitTrackDataDetails() throws ApiException {
        DebitTrackData trackData = new DebitTrackData();
        trackData.setValue(";4024720012345671=18125025432198712345?");
        trackData.setPinBlock("AFEC374574FC90623D010000116001EE");
        trackData.setEntryMethod(EntryMethod.Swipe);

        String tagData = "82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019";

        Transaction response =
                trackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void DebitSaleContactlessChip_NewDebitTrackDataDetails() throws ApiException {
        DebitTrackData trackData = new DebitTrackData();
        trackData.setValue(";4024720012345671=18125025432198712345?");
        trackData.setPinBlock("AFEC374574FC90623D010000116001EE");
        trackData.setEntryMethod(EntryMethod.Proximity);

        String tagData = "82021C008407A0000002771010950580000000009A031709289C01005F280201245F2A0201245F3401019F02060000000010009F03060000000000009F080200019F090200019F100706010A03A420009F1A0201249F26089CC473F4A4CE18D39F2701809F3303E0F8C89F34030100029F3501229F360200639F370435EFED379F410400000019";

        Transaction response =
                trackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void DebitSwipe_Reverse() throws ApiException {
        Transaction response =
                debitTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);

        Transaction reverse =
                response
                        .reverse(amount)
                        .execute();
        assertTransactionResponse(reverse, TransactionStatus.Reversed);
    }

    @Test
    public void DebitRefundChip_Rejected() throws ApiException {
        boolean exceptionCaught = false;
        try {
            debitTrackData
                    .refund(amount)
                    .withCurrency(currency)
                    .withTagData(tagData)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40029", ex.getResponseText());
            assertEquals("Status Code: 400 - 34,Transaction rejected because the provided data was invalid. Online PINBlock Authentication not supported on offline transaction.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @AfterEach
    public void generalValidations() {
        assertEquals("Visa", debitTrackData.getCardType());
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }
}
