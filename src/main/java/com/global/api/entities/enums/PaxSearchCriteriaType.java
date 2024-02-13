package com.global.api.entities.enums;

public enum PaxSearchCriteriaType implements IStringConstant{

     TRANSACTION_TYPE("transactionType"),
     CARD_TYPE( "cardType"),
     RECORD_NUMBER ("recordNumber"),
     TERMINAL_REFERENCE_NUMBER( "terminalReferenceNumber"),
     AUTH_CODE( "authNumber"),
     REFERENCE_NUMBER( "referenceNumber"),
     MERCHANT_ID ( "merchantId"),
     MERCHANT_NAME ( "merchantName");

    String value;
    PaxSearchCriteriaType(String value) { this.value = value; }
    public byte[] getBytes() { return value.getBytes(); }
    public String getValue() { return value; }
}
