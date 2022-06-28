package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum TransactionCode implements IStringConstant {

    Purchase("Purchase","00"), //online or SAF or FloorLimit
    SAFReturnVoid("SAFReturnVoid","00"),
    PreAuthorization("PreAuthorization","01"),
    IncrementalAuth("IncrementalAuth","01"),
    PreAuthorizationCompletion("PreAuthorizationCompletion","02"),
    TelephoneAuthPurchase("TelephoneAuthPurchase","02"),
    SAFPurchaseVoid("SAFPurchaseVoid","04"),
    Return("Return","04"),//online & SAF
    OnlinePurchaseVoid("OnlinePurchaseVoid","11"),
    OnlineReturnVoid("OnlineReturnVoid","12"),
    EmvCashAdvance("EmvCashAdvance","05"),
    EndOfBatch("EndOfBatch","60"),
    EndOfPeriod("EndOfPeriod","62");

    String transactionName;
    String value;
    TransactionCode(String transactionName,String value) {
        this.transactionName=transactionName;
        this.value=value;
    }
    public String getValue() { return this.value;}
    public String getTransactionName(){return  this.transactionName;}
    public byte[] getBytes() { return this.value.getBytes(); }

}
