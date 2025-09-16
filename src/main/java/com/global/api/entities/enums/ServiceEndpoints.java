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
    GP_API_QA("https://apis-uat.globalpay.com/ucp"),
    BILLPAY_TEST("https://testing.heartlandpaymentservices.net/"),
    BILLPAY_CERTIFICATION("https://staging.heartlandpaymentservices.net/"),
    BILLPAY_PRODUCTION("https://heartlandpaymentservices.net/"),
    TRANSACTION_API_PRODUCTION("https://api.paygateway.com/transactions"),
    TRANSACTION_API_TEST("https://api.pit.paygateway.com/transactions"),
    OPEN_BANKING_TEST("https://api.sandbox.globalpay-ecommerce.com/openbanking"),
    OPEN_BANKING_PRODUCTION("https://api.globalpay-ecommerce.com/openbanking"),
    DIAMOND_CLOUD_TEST("https://qr-cert.simpletabcloud.com/tomcat/command"),
    DIAMOND_CLOUD_PROD("https://qr.simpletabcloud.com/tomcat/command"),
    DIAMOND_CLOUD_PROD_EU("https://qreu.simpletabcloud.com/tomcat/command"),
    GENIUS_API_PRODUCTION("https://ps1.merchantware.net/Merchantware/ws/RetailTransaction/v46/Credit.asmx"),
    GENIUS_API_TEST("https://ps1.merchantware.net/Merchantware/ws/RetailTransaction/v46/Credit.asmx"),
    GENIUS_TERMINAL_PRODUCTION("https://transport.merchantware.net/v4/transportService.asmx"),
    GENIUS_TERMINAL_TEST("https://transport.merchantware.net/v4/transportService.asmx"),
    GENIUS_MITC_PRODUCTION("https://api.paygateway.com/transactions"),
    GENIUS_MITC_TEST("https://api.pit.paygateway.com/transactions"),
    TRANSIT_MULTIPASS_PRODUCTION("https://gateway.transit-pass.com/servlets/TransNox_API_Server"),
    TRANSIT_MULTIPASS_TEST("https://stagegw.transnox.com/servlets/TransNox_API_Server"),
    PROPAY_TEST("https://xmltest.propay.com/API/PropayAPI.aspx"),
    PROPAY_TEST_CANADIAN("https://xmltestcanada.propay.com/API/PropayAPI.aspx"),
    PROPAY_PRODUCTION("https://epay.propay.com/API/PropayAPI.aspx"),
    PROPAY_PRODUCTION_CANADIAN("https://www.propaycanada.ca/API/PropayAPI.aspx");

    String value;

    ServiceEndpoints(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getBytes() {
        return this.value.getBytes();
    }
}