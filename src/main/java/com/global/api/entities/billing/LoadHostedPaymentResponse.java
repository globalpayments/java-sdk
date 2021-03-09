package com.global.api.entities.billing;

public class LoadHostedPaymentResponse extends BillingResponse {
    /**
     * Unique identifier for the hosted payment page
     */
    protected String paymentIdentifier;
    
    public String getPaymentIdentifier() {
        return paymentIdentifier;
    }

    public void setPaymentIdentifier(String paymentIdentifier) {
        this.paymentIdentifier = paymentIdentifier;
    }
}
