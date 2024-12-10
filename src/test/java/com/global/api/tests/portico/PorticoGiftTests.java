package com.global.api.tests.portico;

import com.global.api.ServicesContainer;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.serviceConfigs.PorticoConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class PorticoGiftTests {
    private GiftCard card;
    private GiftCard track;

    public PorticoGiftTests() throws ApiException {
        PorticoConfig config = new PorticoConfig();
        config.setSecretApiKey("skapi_cert_MaePAQBr-1QAqjfckFC8FTbRTT120bVQUlfVOjgCBw");
        config.setServiceUrl("https://cert.api2.heartlandportico.com");

        ServicesContainer.configureService(config);

        card = new GiftCard();
        card.setNumber("5022440000000000007");

        track = new GiftCard();
        track.setTrackData("%B5022440000000000098^^391200081613?;5022440000000000098=391200081613?");
    }

    @Test
    public void giftCreate() throws ApiException {
        GiftCard newCard = GiftCard.create("2145550199");
        assertNotNull(newCard);
        assertNotNull(newCard.getNumber());
        assertNotNull(newCard.getAlias());
        assertNotNull(newCard.getPin());
    }

    @Test
    public void giftAddAlias() throws ApiException {
        Transaction response = card.addAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftAddValue() throws ApiException {
        Transaction response = card.addValue(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftBalanceInquiry() throws ApiException {
        Transaction response = card.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftSale() throws ApiException {
        Transaction response = card.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftDeactivate() throws ApiException {
        Transaction response = card.deactivate().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftRemoveAlias() throws ApiException {
        Transaction response = card.removeAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftReplace() throws ApiException {
        Transaction response = card.replaceWith(track).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftReverse() throws ApiException {
        Transaction response = card.reverse(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftRewards() throws ApiException {
        Transaction response = card.rewards(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackAddAlias() throws ApiException {
        Transaction response = track.addAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackAddValue() throws ApiException {
        Transaction response = track.addValue(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackBalanceInquiry() throws ApiException {
        Transaction response = track.balanceInquiry().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackSale() throws ApiException {
        Transaction response = track.charge(new BigDecimal("10"))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackDeactivate() throws ApiException {
        Transaction response = track.deactivate().execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackRemoveAlias() throws ApiException {
        Transaction response = track.removeAlias("2145550199").execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackReplace() throws ApiException {
        Transaction response = track.replaceWith(card).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackReverse() throws ApiException {
        Transaction response = track.reverse(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftTrackRewards() throws ApiException {
        Transaction response = track.rewards(new BigDecimal("10")).execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());
    }

    @Test
    public void giftReverseWithTransactionId() throws ApiException {
        Transaction response = card.charge(new BigDecimal(10))
                .withCurrency("USD")
                .execute();
        assertNotNull(response);
        assertEquals("00", response.getResponseCode());

        Transaction reverseResponse = Transaction.fromId(response.getTransactionId(), PaymentMethodType.Gift)
                .reverse(new BigDecimal(10))
                .execute();
        assertNotNull(reverseResponse);
        assertEquals("00", reverseResponse.getResponseCode());
    }
}
