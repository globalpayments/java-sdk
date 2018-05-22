package com.global.api.serviceConfigs;

import com.global.api.entities.enums.FraudFilterMode;
import com.global.api.entities.enums.HppVersion;

public class HostedPaymentConfig {
    private Boolean cardStorageEnabled;
    private Boolean dynamicCurrencyConversionEnabled;
    private Boolean displaySavedCards;
    private FraudFilterMode fraudFilterMode = FraudFilterMode.None;
    private String language;
    private String paymentButtonText;
    private String postDimensions;
    private String postResponse;
    private String responseUrl;
    private Boolean requestTransactionStabilityScore;
    private HppVersion version;

    public Boolean isCardStorageEnabled() {
        return cardStorageEnabled;
    }
    public void setCardStorageEnabled(boolean cardStorageEnabled) {
        this.cardStorageEnabled = cardStorageEnabled;
    }
    public Boolean isDynamicCurrencyConversionEnabled() {
        return dynamicCurrencyConversionEnabled;
    }
    public void setDynamicCurrencyConversionEnabled(boolean directCurrencyConversionEnabled) {
        this.dynamicCurrencyConversionEnabled = directCurrencyConversionEnabled;
    }
    public Boolean isDisplaySavedCards() {
        return displaySavedCards;
    }
    public void setDisplaySavedCards(boolean displaySavedCards) {
        this.displaySavedCards = displaySavedCards;
    }
    public FraudFilterMode getFraudFilterMode() {
        return fraudFilterMode;
    }
    public void setFraudFilterMode(FraudFilterMode fraudFilterMode) {
        this.fraudFilterMode = fraudFilterMode;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public String getPaymentButtonText() {
        return paymentButtonText;
    }
    public void setPaymentButtonText(String paymentButtonText) {
        this.paymentButtonText = paymentButtonText;
    }
    public String getPostDimensions() {
        return postDimensions;
    }
    public void setPostDimensions(String postDimensions) {
        this.postDimensions = postDimensions;
    }
    public String getPostResponse() {
        return postResponse;
    }
    public void setPostResponse(String postResponse) {
        this.postResponse = postResponse;
    }
    public String getResponseUrl() {
        return responseUrl;
    }
    public void setResponseUrl(String responseUrl) {
        this.responseUrl = responseUrl;
    }
    public Boolean isRequestTransactionStabilityScore() {
        return requestTransactionStabilityScore;
    }
    public void setRequestTransactionStabilityScore(boolean requestTransactionStabilityScore) {
        this.requestTransactionStabilityScore = requestTransactionStabilityScore;
    }
    public HppVersion getVersion() {
        return version;
    }
    public void setVersion(HppVersion version) {
        this.version = version;
    }
}
