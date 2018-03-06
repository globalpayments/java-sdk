package com.global.api.entities;

import com.global.api.entities.exceptions.ApiException;

import java.math.BigDecimal;

public class ThreeDSecure {
    private int algorithm;
    private BigDecimal amount;
    private String cavv;
    private String currency;
    private String eci;
    private boolean enrolled;
    private String issuerAcsUrl;
    private MerchantDataCollection merchantData;
    private String orderId;
    private String payerAuthenticationRequest;
    private String paymentDataSource;
    private String paymentDataType;
    private String status;
    private String xid;

    public int getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) throws ApiException {
        this.amount = amount;
        getMerchantData().put("_amount", amount != null ? amount.toString() : null, false);
    }
    public String getCavv() {
        return cavv;
    }
    public void setCavv(String cavv) {
        this.cavv = cavv;
    }
    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) throws ApiException {
        this.currency = currency;
        getMerchantData().put("_currency", currency, false);
    }
    public String getEci() {
        return eci;
    }
    public void setEci(String eci) {
        this.eci = eci;
    }
    public boolean isEnrolled() {
        return enrolled;
    }
    public void setEnrolled(boolean enrolled) {
        this.enrolled = enrolled;
    }
    public String getIssuerAcsUrl() {
        return issuerAcsUrl;
    }
    public void setIssuerAcsUrl(String issuerAcsUrl) {
        this.issuerAcsUrl = issuerAcsUrl;
    }
    public MerchantDataCollection getMerchantData() {
        if(merchantData == null)
            merchantData = new MerchantDataCollection();
        return merchantData;
    }
    public void setMerchantData(MerchantDataCollection merchantData) {
        if(this.merchantData != null)
            merchantData.mergeHidden(this.merchantData);

        this.merchantData = merchantData;
        if(merchantData.hasKey("_amount"))
            this.amount = merchantData.getDecimal("_amount");
        if(merchantData.hasKey("_currency"))
            this.currency = merchantData.getString("_currency");
        if(merchantData.hasKey("_orderId"))
            this.orderId = merchantData.getString("_orderId");
    }
    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) throws ApiException {
        this.orderId = orderId;
        getMerchantData().put("_orderId", orderId, false);
    }
    public String getPayerAuthenticationRequest() {
        return payerAuthenticationRequest;
    }
    public void setPayerAuthenticationRequest(String payerAuthenticationRequest) {
        this.payerAuthenticationRequest = payerAuthenticationRequest;
    }
    public String getPaymentDataSource() {
        return paymentDataSource;
    }
    public void setPaymentDataSource(String paymentDataSource) {
        this.paymentDataSource = paymentDataSource;
    }
    public String getPaymentDataType() {
        return paymentDataType;
    }
    public void setPaymentDataType(String paymentDataType) {
        this.paymentDataType = paymentDataType;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }

    public ThreeDSecure() {
        this.paymentDataType = "3DSecure";
    }
}
