package com.global.api.tests.utils;

import com.global.api.paymentMethods.CreditTrackData;
import com.global.api.paymentMethods.GiftCard;
import org.junit.Test;

import static org.junit.Assert.*;

public class TrackDataTests {
    @Test
    public void mastercard_track1() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B5473500000000014^MC TEST CARD^251210199998888777766665555444433332?");

        assertEquals("MC", track.getCardType());
        assertEquals("5473500000000014", track.getPan());
        assertEquals("2512", track.getExpiry());
        assertFalse(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void mastercard_track1_truncated() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("B5473500000000014^MC TEST CARD^2512");

        assertEquals("MC", track.getCardType());
        assertEquals("5473500000000014", track.getPan());
        assertEquals("2512", track.getExpiry());
        assertFalse(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void mastercard_track2() {
        CreditTrackData track = new CreditTrackData();
        track.setValue(";5473500000000014=25121019999888877776?");

        assertEquals("MC", track.getCardType());
        assertEquals("5473500000000014", track.getPan());
        assertEquals("2512", track.getExpiry());
        assertFalse(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void mastercard_track2_truncated() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5473500000000014=2512");

        assertEquals("MC", track.getCardType());
        assertEquals("5473500000000014", track.getPan());
        assertEquals("2512", track.getExpiry());
        assertFalse(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void mastercard_fleet() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("5532320000001113=20121019999888877712");

        assertEquals("MCFleet", track.getCardType());
        assertEquals("5532320000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void visa_track1() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?");

        assertEquals("Visa", track.getCardType());
        assertEquals("4012002000060016", track.getPan());
        assertEquals("2512", track.getExpiry());
        assertFalse(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void visa_track2() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("4012002000060016=25121011803939600000");

        assertEquals("Visa", track.getCardType());
        assertEquals("4012002000060016", track.getPan());
        assertEquals("1011803939600000", track.getDiscretionaryData());
        assertEquals("2512", track.getExpiry());
        assertFalse(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void visa_both_tracks() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4012002000060016^VI TEST CREDIT^251210118039000000000396?;4012002000060016=25121011803939600000?");

        assertEquals("Visa", track.getCardType());
        assertEquals("4012002000060016", track.getPan());
        assertEquals("2512", track.getExpiry());
        assertFalse(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void visa_fleet() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B4485531111111118^VISA TEST CARD/GOOD^20121019206100000000003?");

        assertEquals("VisaFleet", track.getCardType());
        assertEquals("4485531111111118", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void wex_fleet_track1() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("%B6900460000001113^WEX FLEET TEST^20121019999888877712?");

        assertEquals("WexFleet", track.getCardType());
        assertEquals("6900460000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void wex_fleet_track2() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("6900460000001113=20121019999888877712");

        assertEquals("WexFleet", track.getCardType());
        assertEquals("6900460000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void voyager_fleet_track1() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("B7088890000001113^VOYAGER FLEET TEST^20121019999888877712");

        assertEquals("VoyagerFleet", track.getCardType());
        assertEquals("7088890000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void voyager_fleet_track2() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("7088850000001113=20121019999888877712");

        assertEquals("VoyagerFleet", track.getCardType());
        assertEquals("7088850000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void fuelman_fleet_track2() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("70764912345100040=4912");

        assertEquals("FuelmanFleet", track.getCardType());
        assertEquals("70764912345100040", track.getPan());
        assertEquals("4912", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void fleetWide_fleet_track2() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("70768512345200005=99120");

        assertEquals("FleetWide", track.getCardType()); //FleetWideFleet
        assertEquals("70768512345200005", track.getPan());
        assertEquals("9912", track.getExpiry());
        assertTrue(track.isFleet());
        assertNotNull(track.getTrackData());
    }


    @Test
    public void valueLink_track1() {
        GiftCard track = new GiftCard();
        track.setValue("B6010560000001113^VALUE LINK TEST^20121019999888877712");

        assertEquals("ValueLink", track.getCardType());
        assertEquals("6010560000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void valueLink_track2() {
        GiftCard track = new GiftCard();
        track.setValue("6032250000001113=20121019999888877712");

        assertEquals("ValueLink", track.getCardType());
        assertEquals("6032250000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void manual_giftCard() {
        GiftCard giftCard = new GiftCard();
        giftCard.setValue("2563235076");

        assertEquals("CardNbr", giftCard.getValueType());
        assertEquals("2563235076", giftCard.getPan());
        assertNotNull(giftCard.getNumber());
    }

    @Test
    public void storedValue_track1() {
        GiftCard track = new GiftCard();
        track.setValue("B6006490000001113^STORED VALUE TEST^20121019999888877712");

        assertEquals("StoredValue", track.getCardType());
        assertEquals("6006490000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void storedValue_track2() {
        GiftCard track = new GiftCard();
        track.setValue("6394700000001113=20121019999888877712");

        assertEquals("StoredValue", track.getCardType());
        assertEquals("6394700000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void globalPaymentsGift_track1() {
        GiftCard track = new GiftCard();
        track.setValue("B5022440000001113^GlobalPayments GIFT TEST^20121019999888877712");

        assertEquals("GlobalPaymentsGift", track.getCardType());
        assertEquals("5022440000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void globalPaymentsGift_track2() {
        GiftCard track = new GiftCard();
        track.setValue("70835500000001113=20121019999888877712");

        assertEquals("GlobalPaymentsGift", track.getCardType());
        assertEquals("70835500000001113", track.getPan());
        assertEquals("2012", track.getExpiry());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void hex_encoded_1() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("2223005065780D19121016wImiKusZcUl9y0MF");

        assertEquals("MC", track.getCardType());
        assertEquals("2223005065780", track.getPan());
        assertEquals("1912", track.getExpiry());
        assertEquals("1016wImiKusZcUl9y0M", track.getDiscretionaryData());
        assertEquals(36, track.getPan().length() + track.getExpiry().length() + track.getDiscretionaryData().length());
        assertEquals("2223005065780=19121016wImiKusZcUl9y0M", track.getTrackData());
        assertNotNull(track.getTrackData());
    }

    @Test
    public void hex_encoded_2() {
        CreditTrackData track = new CreditTrackData();
        track.setValue("2223005065780D19121016wImiKusfcUl9y0MF");

        assertEquals("MC", track.getCardType());
        assertEquals("2223005065780", track.getPan());
        assertEquals("1912", track.getExpiry());
        assertEquals("1016wImiKusfcUl9y0M", track.getDiscretionaryData());
        assertEquals(36, track.getPan().length() + track.getExpiry().length() + track.getDiscretionaryData().length());
        assertEquals("2223005065780=19121016wImiKusfcUl9y0M", track.getTrackData());
        assertNotNull(track.getTrackData());
    }
}
