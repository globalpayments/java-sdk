package com.global.api.entities.enums;

public enum ServiceEndpoints implements IStringConstant {
    GLOBAL_ECOM_PRODUCTION("https://api.realexpayments.com/epage-remote.cgi"),
    GLOBAL_ECOM_TEST("https://api.sandbox.realexpayments.com/epage-remote.cgi"),
    PORTICO_PRODUCTION("https://api2.heartlandportico.com"),
    PORTICO_TEST("https://cert.api2.heartlandportico.com"),
    THREE_DS_AUTH_PRODUCTION("https://api.globalpay-ecommerce.com/3ds2/"),
    THREE_DS_AUTH_TEST("https://api.sandbox.globalpay-ecommerce.com/3ds2/"),
    PAYROLL_PRODUCTION("https://taapi.heartlandpayrollonlinetest.com/PosWebUI"),
    PAYROLL_TEST("https://taapi.heartlandpayrollonlinetest.com/PosWebUI/Test/Test"),
    TABLE_SERVICE_PRODUCTION("https://www.freshtxt.com/api31/"),
    TABLE_SERVICE_TEST("https://www.freshtxt.com/api31/"),
    // TODO: Define the final value before going to Production
    GP_API_PRODUCTION("https://TO-BE-DEFINED.COM"),
    GP_API_TEST("https://apis.sandbox.globalpay.com/ucp");

    String value;
    ServiceEndpoints(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
