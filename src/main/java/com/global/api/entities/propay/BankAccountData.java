package com.global.api.entities.propay;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BankAccountData {
    
    /** ISO 3166 standard 3-character country code */
    private String accountCountryCode;
    
    /** Merchant/Individual Name */
    private String accountName;
    
    /** Financial Institution account number */
    private String accountNumber;
    
    /** Valid values are: Personal and Business */
    private String accountOwnershipType;
    
    /** Valid Values are:
    * C - Checking
    * S - Savings
    * G - General Ledger
     */
    private String accountType;
    
    /** Name of financial institution */
    private String bankName;
    
    /** Financial institution routing number. Must be a valid ACH routing number.*/
    private String routingNumber;
    
    /** The account holder's name. This is required if payment method is a bank account.*/
    private String accountHolderName;
}
