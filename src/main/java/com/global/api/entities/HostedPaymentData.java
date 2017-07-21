package com.global.api.entities;

import java.util.HashMap;

public class HostedPaymentData {
    private Boolean customerExists;
    private String customerKey;
    private String customerNumber;
    private Boolean offerToSaveCard;
    private String paymentKey;
    private String productId;
    private HashMap<String, String> supplementaryData;

    public Boolean isCustomerExists() {
        return customerExists;
    }
    public void setCustomerExists(boolean customerExists) {
        this.customerExists = customerExists;
    }
    public String getCustomerKey() {
        return customerKey;
    }
    public void setCustomerKey(String customerKey) {
        this.customerKey = customerKey;
    }
    public String getCustomerNumber() {
        return customerNumber;
    }
    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }
    public Boolean isOfferToSaveCard() {
        return offerToSaveCard;
    }
    public void setOfferToSaveCard(boolean offerToSaveCard) {
        this.offerToSaveCard = offerToSaveCard;
    }
    public String getPaymentKey() {
        return paymentKey;
    }
    public void setPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public HashMap<String, String> getSupplementaryData() {
        return supplementaryData;
    }
    public void setSupplimentaryData(HashMap<String, String> supplementaryData) {
        this.supplementaryData = supplementaryData;
    }

    public HostedPaymentData() {
        supplementaryData = new HashMap<String, String>();
    }
}
