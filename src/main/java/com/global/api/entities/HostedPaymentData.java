package com.global.api.entities;

import com.global.api.entities.billing.Bill;
import com.global.api.entities.enums.AlternativePaymentType;
import com.global.api.entities.enums.ChallengeRequest;
import com.global.api.entities.enums.HostedPaymentType;
import com.global.api.entities.enums.HostedPaymentMethods;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;

public class HostedPaymentData {
    private Boolean addressesMatch;
    private List<Bill> bills;
    private ChallengeRequest challengeRequestIndicator;
    private Boolean customerExists = false;
    private Boolean customerIsEditable;
    private Address customerAddress;
    private String customerEmail;
    private String customerKey;
    private String customerNumber;
    private String customerCountry;
    private String customerFirstName;
    private String customerLastName;
    private String customerPhoneMobile;
    private HostedPaymentType hostedPaymentType;
    private Boolean offerToSaveCard;
    private String paymentKey;
    private String productId;
    @Getter @Setter private Boolean captureAddress;
    // Determines whether the HPP response will contain the address and contact information
    @Getter @Setter private Boolean returnAddress = false;
    @Getter @Setter private Boolean enableExemptionOptimization;
    private AlternativePaymentType[] presetPaymentMethods;
    private HashMap<String, String> supplementaryData;
    private String transactionStatusUrl;
    @Getter @Setter private String cancelUrl;
    private String merchantResponseUrl;
    @Getter @Setter private HostedPaymentMethods[] hostedPaymentMethods;

    public Boolean getAddressesMatch() {
        return addressesMatch;
    }
    public void setAddressesMatch(Boolean addressesMatch) {
        this.addressesMatch = addressesMatch;
    }
    public List<Bill> getBills() {
        return bills;
    }
    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }
    public ChallengeRequest getChallengeRequestIndicator() {
        return challengeRequestIndicator;
    }
    public void setChallengeRequestIndicator(ChallengeRequest challengeRequestIndicator) {
        this.challengeRequestIndicator = challengeRequestIndicator;
    }
    public String getCustomerEmail() {
        return customerEmail;
    }
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    public Boolean isCustomerExists() {
        return customerExists;
    }
    public void setCustomerExists(Boolean customerExists) {
        this.customerExists = customerExists;
    }
    public boolean isCustomerEditable() {
        return customerIsEditable;
    }
    public void setCustomerIsEditable(boolean customerIsEditable) {
        this.customerIsEditable = customerIsEditable;
    }
    public Address getCustomerAddress() {
        return customerAddress;
    }
    public void setCustomerAddress(Address address) {
        this.customerAddress = address;
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
    public String getCustomerCountry() {
		return customerCountry;
	}
	public void setCustomerCountry(String customerCountry) {
		this.customerCountry = customerCountry;
	}
	public String getCustomerFirstName() {
		return customerFirstName;
	}
	public void setCustomerFirstName(String customerFirstName) {
		this.customerFirstName = customerFirstName;
	}
	public String getCustomerLastName() {
		return customerLastName;
	}
	public void setCustomerLastName(String customerLastName) {
		this.customerLastName = customerLastName;
	}
    public String getCustomerPhoneMobile() {
        return customerPhoneMobile;
    }
    public void setCustomerPhoneMobile(String customerPhoneMobile) {
        this.customerPhoneMobile = customerPhoneMobile;
    }
    public String getMerchantResponseUrl() {
		return merchantResponseUrl;
	}
	public void setMerchantResponseUrl(String merchantResponseUrl) {
		this.merchantResponseUrl = merchantResponseUrl;
    }
    public HostedPaymentType getHostedPaymentType() {
        return hostedPaymentType;
    }
    public void setHostedPaymentType(HostedPaymentType hostedPaymentType) {
        this.hostedPaymentType = hostedPaymentType;
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
	public AlternativePaymentType[] getPresetPaymentMethods() {
		return presetPaymentMethods;
	}
	public void setPresetPaymentMethods(AlternativePaymentType ... paymentTypes) {
		this.presetPaymentMethods = paymentTypes;
	}
	public HashMap<String, String> getSupplementaryData() {
        return supplementaryData;
    }
    public void setSupplimentaryData(HashMap<String, String> supplementaryData) {
        this.supplementaryData = supplementaryData;
    }
    public String getTransactionStatusUrl() {
		return transactionStatusUrl;
	}
	public void setTransactionStatusUrl(String transactionStatusUrl) {
		this.transactionStatusUrl = transactionStatusUrl;
	}
	public HostedPaymentData() {
        supplementaryData = new HashMap<>();
    }
}
