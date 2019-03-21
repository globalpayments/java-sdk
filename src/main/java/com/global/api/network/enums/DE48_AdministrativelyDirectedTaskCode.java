package com.global.api.network.enums;

import com.global.api.entities.enums.IByteConstant;

public enum DE48_AdministrativelyDirectedTaskCode implements IByteConstant {
    NoNotification(0x30),
    MailPending(0x31),
    ParameterDataLoad_Pending(0x32),
    EMV_ParameterDataLoad_Pending(0x33),
    MultipleMessagesPending(0x34);

    private final int value;
    DE48_AdministrativelyDirectedTaskCode(int value) { this.value = value; }
    public byte getByte() { return (byte)value; }
}
