package com.example.restservice.entities.initiateAuthentication;

import com.example.restservice.entities.MobileData;
import com.global.api.entities.Address;
import com.global.api.entities.BrowserData;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.enums.AuthenticationRequestType;
import com.global.api.entities.enums.AuthenticationSource;
import com.global.api.entities.enums.ChallengeRequestIndicator;
import com.global.api.entities.enums.MessageCategory;
import com.global.api.entities.enums.MethodUrlCompletion;

public class InitiateAuthenticationInput {

	private String cardToken;
	private String amount;
	private String currency;
	private MobileData mobileData;
	private ThreeDSecure threeDsecure;
	private Address billingAddress;
	private Address shippingAddress;
	private boolean addressMatchIndicator;
    private String customerEmail;
    private AuthenticationSource authenticationSource;
    private AuthenticationRequestType authenticationRequestType;
    private MessageCategory messageCategory;
    private ChallengeRequestIndicator challengeRequestIndicator;
    private BrowserData browserData;
    private MethodUrlCompletion methodUrlCompletion;
	private boolean preferredDecoupledAuth;
	private Integer decoupledFlowTimeout;

	public InitiateAuthenticationInput() {

	}
	
	public String getCardToken() {
		return cardToken;
	}

	public String getAmount() {
		return amount;
	}

	public String getCurrency() {
		return currency;
	}

	public MobileData getMobileData() {
		return mobileData;
	}

	public ThreeDSecure getThreeDsecure() {
		return threeDsecure;
	}

	public Address getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}

	public Address getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(Address shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public boolean getAddressMatchIndicator() {
		return addressMatchIndicator;
	}

	public void setAddressMatchIndicator(boolean addressMatchIndicator) {
		this.addressMatchIndicator = addressMatchIndicator;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
	}

	public AuthenticationSource getAuthenticationSource() {
		return authenticationSource;
	}

	public void setAuthenticationSource(AuthenticationSource authenticationSource) {
		this.authenticationSource = authenticationSource;
	}

	public AuthenticationRequestType getAuthenticationRequestType() {
		return authenticationRequestType;
	}

	public void setAuthenticationRequestType(AuthenticationRequestType authenticationRequestType) {
		this.authenticationRequestType = authenticationRequestType;
	}

	public MessageCategory getMessageCategory() {
		return messageCategory;
	}

	public void setMessageCategory(MessageCategory messageCategory) {
		this.messageCategory = messageCategory;
	}

	public ChallengeRequestIndicator getChallengeRequestIndicator() {
		return challengeRequestIndicator;
	}

	public void setChallengeRequestIndicator(ChallengeRequestIndicator challengeRequestIndicator) {
		this.challengeRequestIndicator = challengeRequestIndicator;
	}

	public BrowserData getBrowserData() {
		return browserData;
	}

	public void setBrowserData(BrowserData browserData) {
		this.browserData = browserData;
	}

	public MethodUrlCompletion getMethodUrlCompletion() {
		return methodUrlCompletion;
	}

	public void setMethodUrlCompletion(MethodUrlCompletion methodUrlCompletion) {
		this.methodUrlCompletion = methodUrlCompletion;
	}

	public void setCardToken(String cardToken) {
		this.cardToken = cardToken;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setMobileData(MobileData mobileData) {
		this.mobileData = mobileData;
	}

	public void setThreeDsecure(ThreeDSecure threeDsecure) {
		this.threeDsecure = threeDsecure;
	}

	public boolean getPreferredDecoupledAuth() {
		return preferredDecoupledAuth;
	}

	public void setPreferredDecoupledAuth(boolean preferredDecoupledAuth) {
		this.preferredDecoupledAuth = preferredDecoupledAuth;
	}

	public Integer getDecoupledFlowTimeout() {
		return decoupledFlowTimeout;
	}

	public void setDecoupledFlowTimeout(Integer decoupledFlowTimeout) {
		this.decoupledFlowTimeout = decoupledFlowTimeout;
	}

}