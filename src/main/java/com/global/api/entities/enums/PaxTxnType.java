package com.global.api.entities.enums;

public enum PaxTxnType implements IStringConstant {
    MENU("00"),
    SALE_REDEEM("01"),
    RETURN("02"),
    AUTH("03"),
    POSTAUTH("04"),
    FORCED("05"),
    ADJUST("06"),
    WITHDRAWAL("07"),
    ACTIVATE("08"),
    ISSUE("09"),
    ADD("10"),
    CASHOUT("11"),
    DEACTIVATE("12"),
    REPLACE("13"),
    MERGE("14"),
    REPORTLOST("15"),
    VOID("16"),
    V_SALE("17"),
    V_RTRN("18"),
    V_AUTH("19"),
    V_POST("20"),
    V_FRCD("21"),
    V_WITHDRAW("22"),
    BALANCE("23"),
    VERIFY("24"),
    REACTIVATE("25"),
    FORCED_ISSUE("26"),
    FORCED_ADD("27"),
    UNLOAD("28"),
    RENEW("29"),
    GET_CONVERT_DETAIL("30"),
    CONVERT("31"),
    TOKENIZE("32"),
    REVERSAL("99");

    String value;
    PaxTxnType(String value) { this.value = value; }
    public byte[] getBytes() { return value.getBytes(); }
    public String getValue() { return value; }
}