package com.global.api.network.enums.gnap;

import com.global.api.entities.enums.IStringConstant;

public enum PaymentSolutionProviderCode implements IStringConstant {

    Tender_Retail("TDR"),
    Bulloch("BUL"),
    Eigen("EIG"),
    Aurus("AUR") ,
    Infonet("INF"),
    Shift4("SH4"),
    Software("LOC"),
    Verifone("FIP"),
    Howell_Data("HOW") ;

    String value;
    PaymentSolutionProviderCode(String value){this.value=value;}

    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
