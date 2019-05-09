package com.global.api.entities.enums;

public enum HpaMsgId implements IStringConstant {
    LANE_OPEN("LaneOpen"),
    LANE_CLOSE("LaneClose"),
    RESET("Reset"),
    REBOOT("Reboot"),
    BATCH_CLOSE("CloseBatch"),
    GET_BATCH_REPORT("GetBatchReport"),
    CREDIT_SALE("Sale"),
    CREDIT_REFUND("Refund"),
    CREDIT_VOID("Void"),
    CARD_VERIFY("CardVerify"),
    CREDIT_AUTH("CreditAuth"),
    BALANCE("BalanceInquiry"),
    ADD_VALUE("AddValue"),
    TIP_ADJUST("TipAdjust"),
    GET_INFO_REPORT("GetAppInfoReport"),
    SIGNATURE_FORM("SIGNATUREFORM"),
    START_CARD("StartCard"),
    LINE_ITEM("LineItem"),
    SEND_SAF("SendSAF"),
    GET_PARAMETER_REPORT("GetParameterReport"),
    SET_PARAMETER("SetParameter");

    String value;
    HpaMsgId(String value) { this.value = value; }
    public byte[] getBytes() { return value.getBytes(); }
    public String getValue() { return value; }
}
