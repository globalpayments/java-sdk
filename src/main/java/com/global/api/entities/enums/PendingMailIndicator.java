package com.global.api.entities.enums;

public enum PendingMailIndicator implements IStringConstant {
    MailWaiting("M"),
    NoMail("O");

    String value;
    PendingMailIndicator(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }

    }
