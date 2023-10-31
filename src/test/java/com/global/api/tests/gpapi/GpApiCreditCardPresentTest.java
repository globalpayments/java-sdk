package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.EncryptionData;
import com.global.api.entities.LodgingData;
import com.global.api.entities.LodgingItems;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.entities.reporting.SearchCriteria;
import com.global.api.entities.reporting.TransactionSummaryPaged;
import com.global.api.paymentMethods.CreditCardData;
import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import com.global.api.services.ReportingService;
import com.global.api.utils.DateUtils;
import lombok.SneakyThrows;
import lombok.var;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GpApiCreditCardPresentTest extends BaseGpApiTest {

    private final CreditTrackData creditTrackData = new CreditTrackData();
    private final CreditCardData card = new CreditCardData();
    private final BigDecimal amount = new BigDecimal("12.02");
    private final String currency = "USD";
    private final String tagData;

    public GpApiCreditCardPresentTest() throws ApiException {
        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardPresent);
        ServicesContainer.configureService(config);

        creditTrackData.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        creditTrackData.setPinBlock("32539F50C245A6A93D123412324000AA");
        creditTrackData.setEntryMethod(EntryMethod.Swipe);

        tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390105FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";

        card.setNumber("4263970000005262");
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setCvn("123");
        card.setCardPresent(true);
    }

    //region Create sale using Credit track data
    @Test
    public void CreditTrackData_SaleSwipe() throws ApiException {
        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditTrackData_SaleSwipe_Chip() throws ApiException {
        creditTrackData.setPinBlock(null);
        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditTrackData_SaleSwipe_Chip_WrongPin() throws ApiException {
        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertNotNull(response);
        assertEquals(DECLINED, response.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseMessage());
        assertEquals("55", response.getTransactionReference().getAuthCode());
    }

    @Test
    public void CreditTrackData_SaleSwipe_AuthorizeThenCapture() throws ApiException {
        Transaction response =
                creditTrackData
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
    public void CreditTrackData_RefundSwipe() throws ApiException {
        Transaction response =
                creditTrackData
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditTrackData_RefundChip() throws ApiException {
        creditTrackData.setPinBlock(null);

        Transaction response =
                creditTrackData
                        .refund(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditTrackData_SwipeEncrypted() throws ApiException {
        creditTrackData.setValue("&lt;E1050711%B4012001000000016^VI TEST CREDIT^251200000000000000000000?|LO04K0WFOmdkDz0um+GwUkILL8ZZOP6Zc4rCpZ9+kg2T3JBT4AEOilWTI|+++++++Dbbn04ekG|11;4012001000000016=25120000000000000000?|1u2F/aEhbdoPixyAPGyIDv3gBfF|+++++++Dbbn04ekG|00|||/wECAQECAoFGAgEH2wYcShV78RZwb3NAc2VjdXJlZXhjaGFuZ2UubmV0PX50qfj4dt0lu9oFBESQQNkpoxEVpCW3ZKmoIV3T93zphPS3XKP4+DiVlM8VIOOmAuRrpzxNi0TN/DWXWSjUC8m/PI2dACGdl/hVJ/imfqIs68wYDnp8j0ZfgvM26MlnDbTVRrSx68Nzj2QAgpBCHcaBb/FZm9T7pfMr2Mlh2YcAt6gGG1i2bJgiEJn8IiSDX5M2ybzqRT86PCbKle/XCTwFFe1X|>;");
        creditTrackData.setEncryptionData(EncryptionData.version1());

        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertNotNull(response);
        assertEquals(DECLINED, response.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseMessage());
        assertEquals("14", response.getTransactionReference().getAuthCode());
    }

    @Test
    public void CreditTrackData_SaleSwipeChip_ExpiredCreditTrackDataDetails() throws ApiException {
        CreditTrackData trackData = new CreditTrackData();
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
        assertNotNull(response);
        assertEquals(DECLINED, response.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseMessage());
        assertEquals("54", response.getTransactionReference().getAuthCode());
    }

    @Test
    public void CreditTrackData_SaleContactlessChip() throws ApiException {
        creditTrackData.setPinBlock(null);
        creditTrackData.setEntryMethod(EntryMethod.Proximity);

        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Ignore
    @Test
    public void CreditTrackData_SaleContactlessSwipe() throws ApiException {
        //TODO - determine why entry mode is not set as CONTACTLESS_SWIPE
        creditTrackData.setPinBlock(null);

        String tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390191FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";
        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditTrackData_SaleContactlessChip_WrongPin() throws ApiException {
        creditTrackData.setEntryMethod(EntryMethod.Proximity);

        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .execute();
        assertNotNull(response);
        assertEquals(DECLINED, response.getResponseCode());
        assertEquals(TransactionStatus.Declined.getValue(), response.getResponseMessage());
        assertEquals("55", response.getTransactionReference().getAuthCode());
    }

    @Test
    public void CreditTrackData_SaleSwipe_Refund() throws ApiException {
        Transaction response =
                creditTrackData
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);

        Transaction refund =
                response
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(refund, TransactionStatus.Captured);
    }

    @Test
    public void CreditTrackData_SaleSwipe_Reverse() throws ApiException {
        Transaction response =
                creditTrackData
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
    public void CreditTrackData_RefundChip_Rejected() throws ApiException {
        boolean exceptionCaught = false;
        try {
            creditTrackData
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

    @Test
    public void AdjustSaleTransaction() throws ApiException {
        var card = new CreditTrackData();
        card.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        card.setEntryMethod(EntryMethod.Proximity);

        var tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390105FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";

        Transaction transaction =
                card
                        .charge(10)
                        .withCurrency("USD")
                        .withAllowDuplicates(true)
                        .withTagData(tagData)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .edit()
                        .withAmount(new BigDecimal("10.01"))
                        .withTagData(tagData)
                        .withGratuity(new BigDecimal("5.01"))
                        .execute();

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void AdjustAuthTransaction() throws ApiException {
        var card = initCreditTrackData(EntryMethod.Proximity);
        var tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390105FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";

        Transaction transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .withAllowDuplicates(true)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Preauthorized);

        Transaction response =
                transaction
                        .edit()
                        .withAmount(new BigDecimal("10.01"))
                        .withTagData(tagData)
                        .withGratuity(new BigDecimal("5.01"))
                        .withMultiCapture(1, 1)
                        .execute();

        assertTransactionResponse(response, TransactionStatus.Preauthorized);
    }

    @Test
    public void AdjustSaleTransaction_AdjustAmountHigherThanSale() throws ApiException {
        var card = initCreditTrackData();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withAllowDuplicates(true)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .edit()
                        .withAmount(amount.add(new BigDecimal(2)))
                        .execute();

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void AdjustSaleTransaction_AdjustOnlyTag() throws ApiException {
        var card = initCreditTrackData(EntryMethod.Proximity);
        var tagData = "9F4005F000F0A0019F02060000000025009F03060000000000009F2608D90A06501B48564E82027C005F3401019F360200029F0702FF009F0802008C9F0902008C9F34030403029F2701809F0D05F0400088009F0E0508000000009F0F05F0400098005F280208409F390105FFC605DC4000A800FFC7050010000000FFC805DC4004F8009F3303E0B8C89F1A0208409F350122950500000080005F2A0208409A031409109B02E8009F21030811539C01009F37045EED3A8E4F07A00000000310109F0607A00000000310108407A00000000310109F100706010A03A400029F410400000001";

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .withTagData(tagData)
                        .withAllowDuplicates(true)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .edit()
                        .withTagData(tagData)
                        .execute();

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void AdjustSaleTransaction_AdjustOnlyGratuity() throws ApiException {
        var card = initCreditTrackData();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        //.withChipCondition(EmvLastChipRead.SUCCESSFUL)
                        .withLastChipRead(EmvLastChipRead.SUCCESSFUL)
                        .withAllowDuplicates(true)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .edit()
                        .withGratuity(new BigDecimal("1"))
                        .execute();

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void AdjustSaleTransaction_AdjustAmountToZero() throws ApiException {
        var card = initCreditTrackData();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        //.withChipCondition(EmvLastChipRead.SUCCESSFUL)
                        .withLastChipRead(EmvLastChipRead.SUCCESSFUL)
                        .withAllowDuplicates(true)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .edit()
                        .withAmount(new BigDecimal("0"))
                        .execute();

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void AdjustSaleTransaction_AdjustGratuityToZero() throws ApiException {
        var card = initCreditTrackData();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        //.withChipCondition(EmvLastChipRead.SUCCESSFUL)
                        .withLastChipRead(EmvLastChipRead.SUCCESSFUL)
                        .withAllowDuplicates(true)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .edit()
                        .withGratuity(new BigDecimal("0"))
                        .execute();

        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void AdjustSaleTransaction_WithoutMandatory() throws ApiException {
        var card = initCreditTrackData();

        Transaction transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        //.withChipCondition(EmvLastChipRead.SUCCESSFUL)
                        .withLastChipRead(EmvLastChipRead.SUCCESSFUL)
                        .withAllowDuplicates(true)
                        .execute();

        assertTransactionResponse(transaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;

        try {
            transaction
                    .edit()
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40005", e.getResponseText());
            assertEquals("Status Code: 400 - Request expects the following fields [amount or tag or gratuityAmount]", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void AdjustSaleTransaction_TransactionNotFound() throws ApiException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());

        boolean exceptionCaught = false;
        try {
            transaction
                    .edit()
                    .execute();
        } catch (GatewayException e) {
            exceptionCaught = true;
            assertEquals("40008", e.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + transaction.getTransactionId() + " not found at this location.", e.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @SneakyThrows
    @Test
    public void CreditSale_WithoutPermissions() {
        String[] permissions = new String[]{"TRN_POST_Capture"};

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardPresent);
        config.setPermissions(permissions);

        final String _WITHOUT_PERMISSIONS = "GpApiConfig_WithoutPermissions";

        ServicesContainer.configureService(config, _WITHOUT_PERMISSIONS);

        boolean exceptionCaught = false;
        try {
            creditTrackData
                    .charge(amount)
                    .withCurrency(currency)
                    .execute(_WITHOUT_PERMISSIONS);
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("40212", ex.getResponseText());
            assertEquals("ACTION_NOT_AUTHORIZED", ex.getResponseCode());
            assertEquals("Status Code: 403 - Permission not enabled to execute action", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    //endregion

    //region Create sale using Credit Card Data
    @Test
    public void CreditCard_SaleManual() throws ApiException {
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditCard_SaleManual_AuthorizeThenCapture() throws ApiException {
        Transaction response =
                card
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
    public void CreditCard_RefundManual() throws ApiException {
        Transaction response =
                card
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void CreditCard_SaleManual_Refund() throws ApiException {
        Transaction response =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(response, TransactionStatus.Captured);

        Transaction refund =
                response
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(refund, TransactionStatus.Captured);
    }

    @Test
    public void CreditCard_SaleManual_Reverse() throws ApiException {
        Transaction response =
                card
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
    //endregion

    //region Verify
    @Test
    public void CreditVerify_CreditTrackDataDetails() throws ApiException {
        Transaction response =
                creditTrackData
                        .verify()
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void CreditVerify_CardNumberDetails() throws ApiException {
        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(VERIFIED, response.getResponseMessage());
    }

    @Test
    public void CreditVerify_CardNumber_ExpiredCard() throws ApiException {
        card.setNumber("4000120000001154");
        card.setExpYear(2021);
        Transaction response =
                card
                        .verify()
                        .withCurrency(currency)
                        .execute();

        assertNotNull(response);
        assertEquals("NOT_VERIFIED", response.getResponseCode());
        assertEquals("NOT_VERIFIED", response.getResponseMessage());
    }

    //endregion

    //region Reauthorize
    @Test
    public void CreditCardReauthorizeTransaction() throws ApiException {
        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(chargeTransaction, TransactionStatus.Captured);

        Transaction reverseTransaction =
                chargeTransaction
                        .reverse(amount)
                        .execute();
        assertTransactionResponse(reverseTransaction, TransactionStatus.Reversed);

        Transaction reAuthTransaction =
                reverseTransaction
                        .reauthorize()
                        .execute();
        assertTransactionResponse(reAuthTransaction, TransactionStatus.Captured);
        assertEquals("00", reAuthTransaction.getAuthorizationCode());
    }

    @Test
    public void CreditCardReauthorizeTransaction_OldExistentSale() throws ApiException {
        Date startDate = DateUtils.addDays(new Date(), -100);
        Date endDate = DateUtils.addDays(new Date(), -1);

        TransactionSummaryPaged resultTransactions =
                ReportingService
                        .findTransactionsPaged(1, 1000)
                        .orderBy(TransactionSortProperty.TimeCreated, SortDirection.Descending)
                        .where(SearchCriteria.StartDate, startDate)
                        .and(SearchCriteria.EndDate, endDate)
                        .and(SearchCriteria.TransactionStatus, TransactionStatus.Preauthorized)
                        .and(SearchCriteria.Channel, Channel.CardPresent)
                        .and(SearchCriteria.CardNumberLastFour, "5262")
                        .execute();

        assertNotNull(resultTransactions);

        if (resultTransactions.results.size() > 0) {

            int random = new Random().nextInt(resultTransactions.results.size());

            Transaction transaction = new Transaction();
            transaction.setBalanceAmount(resultTransactions.results.get(random).getAmount());
            transaction.setTransactionId(resultTransactions.results.get(random).getTransactionId());

            Transaction reverseTransaction =
                    transaction
                            .reverse()
                            .execute();
            assertTransactionResponse(reverseTransaction, TransactionStatus.Reversed);

            Transaction reAuthTransaction =
                    reverseTransaction
                            .reauthorize()
                            .execute();
            assertTransactionResponse(reAuthTransaction, TransactionStatus.Preauthorized);
            assertEquals("00", reAuthTransaction.getAuthorizationCode());
        }
    }

    @Test
    public void CreditCardReauthorizeAuthorizedTransaction() throws ApiException {
        Transaction authTransaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(authTransaction, TransactionStatus.Preauthorized);

        Transaction reverseTransaction =
                authTransaction
                        .reverse(amount)
                        .execute();
        assertTransactionResponse(reverseTransaction, TransactionStatus.Reversed);
        assertEquals(authTransaction.getTransactionId(), reverseTransaction.getTransactionId());

        Transaction reAuthTransaction =
                reverseTransaction
                        .reauthorize()
                        .execute();
        assertTransactionResponse(reAuthTransaction, TransactionStatus.Preauthorized);
        assertEquals("00", reAuthTransaction.getAuthorizationCode());
        assertNotEquals(reverseTransaction.getTransactionId(), reAuthTransaction.getTransactionId());
    }

    @Test
    public void CreditCardReauthorizeTransaction_WithIdempotencyKey() throws ApiException {
        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(chargeTransaction, TransactionStatus.Captured);

        Transaction reverseTransaction =
                chargeTransaction
                        .reverse(amount)
                        .execute();
        assertTransactionResponse(reverseTransaction, TransactionStatus.Reversed);

        String idempotencyKey = UUID.randomUUID().toString();
        Transaction reAuthTransaction =
                reverseTransaction
                        .reauthorize()
                        .withIdempotencyKey(idempotencyKey)
                        .execute();
        assertTransactionResponse(reAuthTransaction, TransactionStatus.Captured);
        assertEquals("00", reAuthTransaction.getAuthorizationCode());

        boolean exceptionCaught = false;
        try {
            reverseTransaction
                    .reauthorize()
                    .withIdempotencyKey(idempotencyKey)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("DUPLICATE_ACTION", ex.getResponseCode());
            assertEquals("40039", ex.getResponseText());
            assertEquals("Status Code: 409 - Idempotency Key seen before: id=" + reAuthTransaction.getTransactionId(), ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCardReauthorizeTransaction_Refund() throws ApiException {
        Transaction refundTransaction =
                card
                        .refund(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(refundTransaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            refundTransaction
                    .reauthorize()
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40213", ex.getResponseText());
            assertEquals("Status Code: 400 - An error occurred on the server.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCardReauthorizeTransaction_SaleWithCapturedStatus() throws ApiException {
        Transaction chargeTransaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();
        assertTransactionResponse(chargeTransaction, TransactionStatus.Captured);

        boolean exceptionCaught = false;
        try {
            chargeTransaction
                    .reauthorize()
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_REQUEST_DATA", ex.getResponseCode());
            assertEquals("40044", ex.getResponseText());
            assertEquals("Status Code: 400 - 36, Invalid original transaction for reauthorization-This error is returned from a CreditAuth or CreditSale if the original transaction referenced by GatewayTxnId cannot be found. This is typically because the original does not meet the criteria for the sale or authorization by GatewayTxnID. This error can also be returned if the original transaction is found, but the card number has been written over with nulls after 30 days.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void CreditCardReauthorizeTransaction_NonExistentId() throws ApiException {
        String randomTransactionId = UUID.randomUUID().toString();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(randomTransactionId);
        transaction.setBalanceAmount(amount);

        boolean exceptionCaught = false;
        try {
            transaction
                    .reauthorize()
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + randomTransactionId + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void IncrementalAuth() throws ApiException {
        var transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        var lodgingInfo = new LodgingData();
        lodgingInfo.setBookingReference("s9RpaDwXq1sPRkbP");
        lodgingInfo.setStayDuration(10);
        lodgingInfo.setCheckInDate(DateTime.now());
        lodgingInfo.setCheckOutDate(new DateTime(DateUtils.addDays(DateTime.now().toDate(), 7)));
        lodgingInfo.setRate(new BigDecimal("13.49"));

        ArrayList<LodgingItems> items = new ArrayList<>();
        items.add(
                new LodgingItems()
                        .setTypes(LodgingItemType.NO_SHOW.toString())
                        .setReference("item_1")
                        .setTotalAmount("13.49")
                        .setPaymentMethodProgramCodes(new String[]{PaymentMethodProgram.ASSURED_RESERVATION.toString()}));
        lodgingInfo.setItems(items);

        transaction =
                transaction
                        .additionalAuth(10)
                        .withCurrency(currency)
                        .withLodgingData(lodgingInfo)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());
        assertEquals(new BigDecimal("22.02"), transaction.getAuthorizedAmount());

        var capture =
                transaction
                        .capture()
                        .execute();

        assertNotNull(capture);
        assertEquals("SUCCESS", capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void IncrementalAuth_WithoutLodgingData() throws ApiException {
        var transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        transaction =
                transaction
                        .additionalAuth(10)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());
        assertEquals(new BigDecimal("22.02"), transaction.getAuthorizedAmount());

        var capture =
                transaction
                        .capture()
                        .execute();

        assertNotNull(capture);
        assertEquals("SUCCESS", capture.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), capture.getResponseMessage());
    }

    @Test
    public void IncrementalAuth_Reverse() throws ApiException {
        var transaction =
                card
                        .authorize(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());

        var lodgingInfo = new LodgingData();
        lodgingInfo.setBookingReference("s9RpaDwXq1sPRkbP");
        lodgingInfo.setStayDuration(10);
        lodgingInfo.setCheckInDate(DateTime.now());
        lodgingInfo.setCheckOutDate(new DateTime(DateUtils.addDays(DateTime.now().toDate(), 7)));
        lodgingInfo.setRate(new BigDecimal("13.49"));

        ArrayList<LodgingItems> items = new ArrayList<>();
        items.add(
                new LodgingItems()
                        .setTypes(LodgingItemType.NO_SHOW.toString())
                        .setReference("item_1")
                        .setTotalAmount("13.49")
                        .setPaymentMethodProgramCodes(new String[]{PaymentMethodProgram.ASSURED_RESERVATION.toString()}));
        lodgingInfo.setItems(items);

        transaction =
                transaction
                        .additionalAuth(10)
                        .withCurrency(currency)
                        .withLodgingData(lodgingInfo)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Preauthorized.getValue(), transaction.getResponseMessage());
        assertEquals(new BigDecimal("22.02"), transaction.getAuthorizedAmount());

        var reversed =
                transaction
                        .reverse()
                        .execute();

        assertNotNull(reversed);
        assertEquals("SUCCESS", reversed.getResponseCode());
        assertEquals(TransactionStatus.Reversed.getValue(), reversed.getResponseMessage());
    }

    @Test
    public void IncrementalAuth_Charge() throws ApiException {
        var transaction =
                card
                        .charge(amount)
                        .withCurrency(currency)
                        .execute();

        assertNotNull(transaction);
        assertEquals("SUCCESS", transaction.getResponseCode());
        assertEquals(TransactionStatus.Captured.getValue(), transaction.getResponseMessage());

        var lodgingInfo = new LodgingData();
        lodgingInfo.setBookingReference("s9RpaDwXq1sPRkbP");
        lodgingInfo.setStayDuration(10);
        lodgingInfo.setCheckInDate(DateTime.now());
        lodgingInfo.setCheckOutDate(new DateTime(DateUtils.addDays(DateTime.now().toDate(), 7)));
        lodgingInfo.setRate(new BigDecimal("13.49"));

        ArrayList<LodgingItems> items = new ArrayList<>();
        items.add(
                new LodgingItems()
                        .setTypes(LodgingItemType.NO_SHOW.toString())
                        .setReference("item_1")
                        .setTotalAmount("13.49")
                        .setPaymentMethodProgramCodes(new String[]{PaymentMethodProgram.ASSURED_RESERVATION.toString()}));
        lodgingInfo.setItems(items);

        boolean exceptionCaught = false;
        try {
            transaction
                    .additionalAuth(10)
                    .withCurrency(currency)
                    .withLodgingData(lodgingInfo)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("INVALID_ACTION", ex.getResponseCode());
            assertEquals("40290", ex.getResponseText());
            assertEquals("Status Code: 400 - Cannot PROCESS Incremental Authorization over a transaction that does not have a status of PREAUTHORIZED.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }

    @Test
    public void IncrementalAuth_RandomTransaction() throws ApiException {
        String randomTransactionId = UUID.randomUUID().toString();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(randomTransactionId);

        var lodgingInfo = new LodgingData();
        lodgingInfo.setBookingReference("s9RpaDwXq1sPRkbP");
        lodgingInfo.setStayDuration(10);
        lodgingInfo.setCheckInDate(DateTime.now());
        lodgingInfo.setCheckOutDate(new DateTime(DateUtils.addDays(DateTime.now().toDate(), 7)));
        lodgingInfo.setRate(new BigDecimal("13.49"));

        ArrayList<LodgingItems> items = new ArrayList<>();
        items.add(
                new LodgingItems()
                        .setTypes(LodgingItemType.NO_SHOW.toString())
                        .setReference("item_1")
                        .setTotalAmount("13.49")
                        .setPaymentMethodProgramCodes(new String[]{PaymentMethodProgram.ASSURED_RESERVATION.toString()}));
        lodgingInfo.setItems(items);

        boolean exceptionCaught = false;
        try {
            transaction
                    .additionalAuth(10)
                    .withCurrency(currency)
                    .withLodgingData(lodgingInfo)
                    .execute();
        } catch (GatewayException ex) {
            exceptionCaught = true;
            assertEquals("RESOURCE_NOT_FOUND", ex.getResponseCode());
            assertEquals("40008", ex.getResponseText());
            assertEquals("Status Code: 404 - Transaction " + randomTransactionId + " not found at this location.", ex.getMessage());
        } finally {
            assertTrue(exceptionCaught);
        }
    }
    //endregion

    @After
    public void generalValidations() {
        assertEquals("Visa", creditTrackData.getCardType());
        assertEquals("Visa", card.getCardType());
    }

    private void assertTransactionResponse(Transaction transaction, TransactionStatus transactionStatus) {
        assertNotNull(transaction);
        assertEquals(SUCCESS, transaction.getResponseCode());
        assertEquals(transactionStatus.getValue(), transaction.getResponseMessage());
    }

    private CreditTrackData initCreditTrackData() {
        return initCreditTrackData(EntryMethod.Swipe);
    }

    private CreditTrackData initCreditTrackData(EntryMethod entryMethod) {
        var card = new CreditTrackData();
        card.setTrackData("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        card.setEntryMethod(entryMethod);

        return card;
    }

}