package com.global.api.paymentMethods;

import com.global.api.entities.enums.PaymentMethodType;

public class AlternatePaymentMethod implements IPaymentMethod {
    private PaymentMethodType paymentMethodType;
    private String returnUrl;
    private String cancelUrl;
    private String statusUpdateUrl;
    private String descriptor;
    private String country;
    private String accountHolderName;

    public AlternatePaymentMethod() {
        this.paymentMethodType = PaymentMethodType.Credit;
    }

    public PaymentMethodType getPaymentMethodType() {
        return paymentMethodType;
    }
    
    public void setPaymentMethodType(PaymentMethodType value) {
        this.paymentMethodType = value;
    }

    public String getReturnUrl() {
        return returnUrl;
    }
    
    public void setReturnUrl(String value) {
        this.returnUrl = value;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }
    
    public void setCancelUrl(String value) {
        this.cancelUrl = value;
    }

    public String getStatusUpdateUrl() {
        return statusUpdateUrl;
    }
    
    public void setStatusUpdateUrl(String value) {
        this.statusUpdateUrl = value;
    }

    public String getDescriptor() {
        return descriptor;
    }
    
    public void setDescriptor(String value) {
        this.descriptor= value;
    }

    public String getCountry() {
        return country;
    }
    
    public void setCountry(String value) {
        this.country = value;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String value) {
        this.accountHolderName = value;
    }
}
