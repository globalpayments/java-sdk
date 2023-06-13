package com.global.api.entities.propay;

import com.global.api.entities.Address;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProPayResponseData {
    private String accountNumber;
    private String recAccountNum;
    private String password;
    private String amount;
    private String transNum;
    private String pending;
    private String secondaryAmount;
    private String secondaryTransNum;
    private String sourceEmail;
    private String authToken;
    private List<BeneficialOwnerDataResult> beneficialOwnerDataResults;

    /** Account Information */
    private String accountStatus;
    private Address physicalAddress ;
    private String affiliation ;
    private String apiReady ;
    private String currencyCode ;
    private String expiration ;
    private String signupDate ;
    private String tier ;
    private String visaCheckoutMerchantID ;
    private String creditCardTransactionLimit ;
    private String creditCardMonthLimit ;
    private String achPaymentPerTranLimit ;
    private String achPaymentMonthLimit ;
    private String creditCardMonthlyVolume ;
    private String achPaymentMonthlyVolume ;
    private String reserveBalance ;
    private String masterPassCheckoutMerchantID ;

    /** Enhanced Account Details */
    private UserPersonalData personalData ;
    private Address homeAddress ;
    private Address mailAddress ;
    private BusinessData businessData ;
    private AccountPermissions accountLimits ;
    private String availableBalance ;
    private String pendingBalance ;
    private AccountBalanceResponseData accountBalance ;
    private BankAccountData primaryBankAccountData ;
    private BankAccountData secondaryBankAccountData ;
    private GrossBillingInformation grossBillingInformation ;

    /** Account balance */
    private String pendingAmount ;
    private String reserveAmount ;
    private AccountBalanceResponseData achOut ;
    private AccountBalanceResponseData flashFunds ;

    /** Portico data
     * A unique transaction ID assigned by the payment facilitator
     */
    private String transactionId ;

    /** A unique sub-merchant account ID assigned by the payment facilitator */
    private String transactionNumber ;
}
