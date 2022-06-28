package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.INumericConstant;

public enum PinPadCommunication implements INumericConstant {
    USB(1),
    Ethernet(2),
    RS32(3);

    int value;
    PinPadCommunication(int value){ this.value=value;}
    public int getValue(){ return this.value;}
}
