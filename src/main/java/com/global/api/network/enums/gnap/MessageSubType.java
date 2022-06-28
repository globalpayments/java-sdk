package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum MessageSubType implements IStringConstant {
    OnlineTransactions("O"),
    StoreAndForwardTransactions("S"),
    ConcentratorReversal("C"),
    TimeoutReversalStoreAndForwardTransaction("A"),//If timeout occurs after sending Store and Forward Transaction Request
    TimeoutReversalOnlineTransaction("T");//If timeout occurs after sending Online transaction Request

    String value;
    MessageSubType(String value) { this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
