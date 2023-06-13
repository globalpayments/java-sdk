package com.global.api.tests.testdata;

import com.global.api.entities.Address;
import com.global.api.entities.propay.FlashFundsPaymentCardData;
import com.global.api.paymentMethods.CreditCardData;

public class TestFundsData {
    public static FlashFundsPaymentCardData getFlashFundsPaymentCardData(){
        FlashFundsPaymentCardData paymentCardData = new FlashFundsPaymentCardData();

            CreditCardData cardData = new CreditCardData();
            cardData.setNumber("4895142232120006");
            cardData.setExpMonth(10);
            cardData.setExpYear(2020);
            cardData.setCvn("022");
            cardData.setCardHolderName("Clint Eastwood");
            paymentCardData.setCreditCard(cardData);

            Address cardHolderAddress = new Address();
            cardHolderAddress.setStreetAddress1("900 Metro Center Blv");
            cardHolderAddress.setCity("San Fransisco");
            cardHolderAddress.setState("CA");
            cardHolderAddress.setPostalCode("94404");
            cardHolderAddress.setCountry("USA");
            paymentCardData.setCardholderAddress(cardHolderAddress);

        return paymentCardData;

    }
}
