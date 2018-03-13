package com.global.api.entities.enums;

public enum Parity {
    None(0),
    Odd(1),
    Even(2);

    int value;
    Parity(int value){ this.value = value; }
    public int getValue() { return this.value; }
}
