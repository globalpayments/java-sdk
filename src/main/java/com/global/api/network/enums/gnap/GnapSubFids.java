package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum GnapSubFids implements IStringConstant {
    //SUB-FIDS
    subFID_B("CVV2/CVC2 Value","B"),
    subFID_E("POS Entry Mode","E"),
    subFID_F("Electronic Commerce, Recurring and Visa MIT Indicator Flags","F"),
    subFID_H("Card Verification Digits Presence indicator and Result","H"),
    subFID_I("Transaction Currency Code (TCC)","I"),
    subFID_L("XID/Transaction Token","L"),
    subFID_O("EMV Request Data","O"),
    subFID_P("EMV Additional Request Data","P"),
    subFID_Q("EMV Response Data","Q"),
    subFID_R("EMV Additional Response Data","R"),
    subFID_S("UnionPay Online PIN DUKPT KSN","S"),
    subFID_T("DUKPT KSN","T"),
    subFID_V("Mastercard Authentication Collection Indicator Request/Response","V"),
    subFID_W("CAVV/AAV Result Code","W"),
    subFID_X("Point of Service Data","X"),
    subFID_Y("Universal Card Authentication","Y"),
    subFID_Z("Mastercard Program Protocol/Directory Server Transaction ID","Z"),
    subFID_q("Paywave Form Factor Indicator","q");

    String fidDesc;
    String value;
    GnapSubFids(String fidDesc,String value) {
        this.fidDesc=fidDesc;
        this.value = value;
    }
    public String getValue() {return this.value;}
    public String getFidDesc(){return this.fidDesc;}
    public byte[] getBytes() { return this.value.getBytes(); }
}
