package com.global.api.entities.enums;

public enum MailIndicatorType implements IStringConstant {
    SendMail("0"),
    ReceivedMail("1"),
    ReceivedMailSendMore("2"),
    NoMoreMail("3"),
    MoreMailPresent("4"),
    UnableToSendMail("5"),
    SendingMailFromTheTerminal("6"),
    ReceivedMailFromTerminal("7");

    String value;
    MailIndicatorType(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
