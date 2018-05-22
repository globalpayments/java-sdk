package com.global.api.entities.enums;

public enum DataBits {
    Seven(7),
    Eight(8);

    int value;
    DataBits(int value){ this.value = value; }
    public int getValue() { return this.value; }
}
