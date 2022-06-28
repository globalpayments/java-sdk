package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum ISOResponseCode implements IStringConstant {
    OfflineApproved("Y1"),
    OfflineDeclined("Z1"),
    ApprovalAfterCardInitiatedReferral("Y2"),
    DeclinedAfterCardInitiatedReferral("Z2"),
    UnableToGoOnlineApproved("Y3"),
    UnableToGoOnlineDeclined("Z3"),
    OnlineApproved("00"),
    ReferralRequestedByIssuer("01"),
    CaptureCard("04"),
    OnlineDeclined("05"),
    NotDeclinedAccountStatusInquiry("85"),
    AuthorizationIssuerSystemUnavailable("91"),
    PartialApproval("10");


    String value;
    ISOResponseCode(String value){this.value=value;}
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

}
