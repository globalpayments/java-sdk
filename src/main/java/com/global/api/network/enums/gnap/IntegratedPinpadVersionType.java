package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.INumericConstant;

public enum IntegratedPinpadVersionType implements INumericConstant {
    UPP(0),
    XPI(1),
    TCPX(2),
    RBA(3),
    EIGEN(4),
    AURUS(5),
    BBPOS(6);

    int value;
    IntegratedPinpadVersionType(int value){ this.value=value;}
    public int getValue(){ return this.value;}
}
