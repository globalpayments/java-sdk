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
    GP_API_PRODUCTION("https://apis.globalpay.com/ucp"),
    GP_API_TEST("https://apis.sandbox.globalpay.com/ucp"),
    BILLPAY_TEST("https://testing.heartlandpaymentservices.net"),
    BILLPAY_CERTIFICATION("https://staging.heartlandpaymentservices.net"),
    BILLPAY_PRODUCTION("https://heartlandpaymentservices.net"),
    TRANSACTION_API_PRODUCTION("https://api.paygateway.com/transactions"),
    TRANSACTION_API_TEST("https://api.pit.paygateway.com/transactions"),
    OPEN_BANKING_TEST("https://api.sandbox.globalpay-ecommerce.com/openbanking"),
    OPEN_BANKING_PRODUCTION("https://api.globalpay-ecommerce.com/openbanking"),
    GENIUS_API_PRODUCTION("https://ps1.merchantware.net/Merchantware/ws/RetailTransaction/v46/Credit.asmx"),
    GENIUS_API_TEST("https://ps1.merchantware.net/Merchantware/ws/RetailTransaction/v46/Credit.asmx"),
    GENIUS_TERMINAL_PRODUCTION("https://transport.merchantware.net/v4/transportService.asmx"),
    GENIUS_TERMINAL_TEST("https://transport.merchantware.net/v4/transportService.asmx");

    String value;
    ServiceEndpoints(String value) { this.value = value; }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
