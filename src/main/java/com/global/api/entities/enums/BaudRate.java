package com.global.api.entities.enums;

public enum BaudRate {
    r38400(38400),
    r57600(57600),
    r19200(19200),
    r115200(115200);

    int value;
    BaudRate(int value) {
        this.value = value;
    }
    public int getValue() { return this.value; }
}
