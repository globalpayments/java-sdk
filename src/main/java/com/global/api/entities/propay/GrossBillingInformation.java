package com.global.api.entities.propay;

import com.global.api.entities.Address;
import com.global.api.paymentMethods.CreditCardData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrossBillingInformation {

    /** Address for the gross settle account on file */
    private Address grossSettleAddress;

    /** Bank account details which required for Gross Billing Info */
    private BankAccountData grossSettleBankData;

    /** Credit card details  required for Gross Billing Info*/
    private CreditCardData grossSettleCreditCardData;

    public GrossBillingInformation() {
        grossSettleAddress = new Address();
        grossSettleBankData = new BankAccountData();
        grossSettleCreditCardData = new CreditCardData();
    }
}
