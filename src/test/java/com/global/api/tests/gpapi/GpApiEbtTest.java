package com.global.api.tests.gpapi;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TransactionStatus;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.EBTCardData;
import com.global.api.paymentMethods.EBTTrackData;
import com.global.api.serviceConfigs.GpApiConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class GpApiEbtTest extends BaseGpApiTest {
    private EBTCardData ebtCardData;
    private EBTTrackData ebtTrackData;

    private final String CURRENCY = "USD";
    private final BigDecimal AMOUNT = new BigDecimal(10);

    public GpApiEbtTest() throws ApiException {

        GpApiConfig config = gpApiSetup(APP_ID, APP_KEY, Channel.CardPresent);
        ServicesContainer.configureService(config);
    }

    @BeforeEach
    public void testInitialize() {
        ebtCardData = new EBTCardData();
        ebtCardData.setNumber("4012002000060016");
        ebtCardData.setExpMonth(expMonth);
        ebtCardData.setExpYear(expYear);
        ebtCardData.setPinBlock("32539F50C245A6A93D123412324000AA");
        ebtCardData.setCardHolderName("Jane Doe");
        ebtCardData.setCardPresent(true);

        ebtTrackData = new EBTTrackData();
        ebtTrackData.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");
        ebtTrackData.setEntryMethod(EntryMethod.Swipe);
        ebtTrackData.setPinBlock("32539F50C245A6A93D123412324000AA");
        ebtTrackData.setCardHolderName("Jane Doe");
    }

    @Test
    public void EbtSale_CardData() throws ApiException {
        Transaction response =
                ebtCardData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtSale_TrackData() throws ApiException {
        Transaction response =
                ebtTrackData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtSaleRefund_CardData() throws ApiException {
        Transaction response =
                ebtCardData
                        .refund(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtSaleRefund_TrackData() throws ApiException {
        Transaction response =
                ebtTrackData
                        .refund(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtTransaction_Refund_TrackData() throws ApiException {
        Transaction transaction =
                ebtTrackData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund()
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtTransaction_Refund_CreditData() throws ApiException {
        Transaction transaction =
                ebtCardData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .refund()
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Captured);
    }

    @Test
    public void EbtTransaction_Reverse_TrackData() throws ApiException {
        Transaction transaction =
                ebtTrackData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .reverse()
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Reversed);
    }

    @Test
    public void EbtTransaction_Reverse_CreditData() throws ApiException {
        Transaction transaction =
                ebtCardData
                        .charge(AMOUNT)
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(transaction, TransactionStatus.Captured);

        Transaction response =
                transaction
                        .reverse()
                        .withCurrency(CURRENCY)
                        .execute();

        assertEbtResponse(response, TransactionStatus.Reversed);
    }

    private void assertEbtResponse(Transaction response, TransactionStatus transactionStatus) {
        assertNotNull(response);
        assertEquals(SUCCESS, response.getResponseCode());
        assertEquals(transactionStatus.getValue(), response.getResponseMessage());
    }

}
