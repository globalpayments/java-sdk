package com.global.api.entities.propay;

import com.global.api.entities.Address;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BusinessData {

    /**The legal name of the business as registered */
    private String businessLegalName;

    /** This field can be used to provide DBA information on an account. ProPay accounts can be configured to display DBA on cc statements. (Note most banks' CC statements allow for 29 characters)*/
    private String doingBusinessAs;

    /** EIN - 9 characters without dashes */
    private String employerIdentificationNumber;

    /**Merchant Category Code */
    private String merchantCategoryCode;

    /** The business' website URL */
    private String websiteURL;

    /** The business' description*/
    private String businessDescription;

    /** The monthly colume of bank card transactions; Value representing the number of pennies in USD, or the number of [currency] without decimals. Defaults to $1000.00 if not sent.*/
    private String monthlyBankCardVolume;

    /** The average amount of an individual transaction; Value representing the number of pennies in USD, or the number of [currency] without decimals. Defaults to $300.00 if not sent.*/
    private String averageTicket;

    /** The highest transaction amount; Value representing the number of pennies in USD, or the number of [currency] without decimals. Defaults to $300.00 if not sent.*/
    private String highestTicket;

    /** The business address*/
    private Address businessAddress;


    public BusinessData() {
        businessAddress = new Address();
    }
}
