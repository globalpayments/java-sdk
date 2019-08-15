package com.global.api.entities.enums;

public enum ServiceEndpoints implements IStringConstant {
    GLOBAL_ECOM_PRODUCTION("https://api.realexpayments.com/epage-remote.cgi"),
    GLOBAL_ECOM_TEST("https://api.sandbox.realexpayments.com/epage-remote.cgi"),
    PORTICO_PRODUCTION("https://api2.heartlandportico.com"),
    PORTICO_TEST("https://cert.api2.heartlandportico.com"),
    THREE_DS_AUTH_PRODUCTION("https://authentications.realexpayments.com/3ds/"),
    THREE_DS_AUTH_TEST("https://authentications.sandbox.realexpayments.com/3ds/"),
    PAYROLL_PRODUCTION("https://taapi.heartlandpayrollonlinetest.com/PosWebUI"),
    PAYROLL_TEST("https://taapi.heartlandpayrollonlinetest.com/PosWebUI/Test/Test"),
    TABLE_SERVICE_PRODUCTION("https://www.freshtxt.com/api31/"),
    TABLE_SERVICE_TEST("https://www.freshtxt.com/api31/");

    String value;
    ServiceEndpoints(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
