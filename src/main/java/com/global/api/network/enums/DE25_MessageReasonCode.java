package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum DE25_MessageReasonCode implements IStringConstant {
    AuthCapture("1376"),
    StandInCapture("1377"),
    VoiceCapture("1378"),
    PinDebit_EBT_Acknowledgement("1379"),
    TimeoutWaitingForResponse("4021"),
    MerchantInitiatedReversal("4351"),
    CustomerInitiatedReversal("4352"),
    CustomerInitiated_PartialApproval("4353"),
    SystemTimeout_Malfunction("4354"),
    ForceVoid_PartialApproval("4355"),
    ForceVoid_ApprovedTransaction("4356");

    private String value;
    DE25_MessageReasonCode(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return getValue().getBytes(); }
}
