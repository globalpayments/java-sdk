package com.global.api.entities.enums;

public enum MerchantInitiatedRequestType implements IStringConstant {
    RecurringTransaction("RECURRING_TRANSACTION"),
    InstallmentTransaction("INSTALLMENT_TRANSACTION"),
    AddCard("ADD_CARD"),
    MaintainCardInformation("MAINTAIN_CARD_INFORMATION"),
    AccountVerification("ACCOUNT_VERIFICATION"),
    SplitOrDelayedShipment("SPLIT_OR_DELAYED_SHIPMENT"),
    TopUp("TOP_UP"),
    MailOrder("MAIL_ORDER"),
    TelephoneOrder("TELEPHONE_ORDER"),
    WhitelistStatusCheck("WHITELIST_STATUS_CHECK"),
    OtherPayment("OTHER_PAYMENT"),
    BillingAgreement("BILLING_AGREEMENT");

    String value;
    MerchantInitiatedRequestType(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
