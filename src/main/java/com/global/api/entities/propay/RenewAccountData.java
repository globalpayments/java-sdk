package com.global.api.entities.propay;

import com.global.api.paymentMethods.CreditCardData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenewAccountData {
    /**
     * Supplying a value will change the account's tier under the affiliation upon renewal
     * If not passed, the tier will not be changed
    */
    private String tier;

    /** Credit Card details */
    private CreditCardData creditCard;

    /** The US zip code of the credit card. 5 or 9 digits without a dash for US cards. Omit for internation credit cards. */
    private String zipCode;

    /** User to pay for an account via ACH and monthly renewal. Financial institution account number.
     *Required if using ACH to pay renewal fee
    */
    private String paymentBankAccountNumber;

    /** Used to pay for an account via ACH and monthly renewal. Financial institution account number.
     *Required if using ACH to pay renewal fee
    */
    private String paymentBankRoutingNumber;

    /** Used to pay for an account via ACH and monthly renewal. Valid values are: Checking and Savings */
    private String paymentBankAccountType;

    public RenewAccountData() {
        creditCard = new CreditCardData();
    }
}
