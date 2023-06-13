package com.global.api.entities.propay;

import com.global.api.entities.Address;
import com.global.api.paymentMethods.CreditCardData;
import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class FlashFundsPaymentCardData {

    /** credit card data which is required for the flash funds */
    private CreditCardData creditCard;

    /** cardholder address */
    private Address cardholderAddress;

    public FlashFundsPaymentCardData() {
        creditCard = new CreditCardData();
        cardholderAddress = new Address();
    }
}
