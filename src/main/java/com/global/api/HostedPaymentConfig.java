package com.global.api;

import com.global.api.entities.enums.FraudFilterMode;
import com.global.api.entities.enums.HppVersion;

public class HostedPaymentConfig {
    private Boolean cardStorageEnabled;
    private Boolean directCurrencyConversionEnabled;
    private Boolean displaySavedCards;
    private FraudFilterMode fraudFilterMode;
    private String language;
    private String paymentButtonText;
    private String responseUrl;
    private Boolean requestTransactionStabilityScore;
    private HppVersion version;

    public Boolean isCardStorageEnabled() {
        return cardStorageEnabled;
    }
    public void setCardStorageEnabled(boolean cardStorageEnabled) {
        this.cardStorageEnabled = cardStorageEnabled;
    }
    public Boolean isDirectCurrencyConversionEnabled() {
        return directCurrencyConversionEnabled;
    }
    public void setDirectCurrencyConversionEnabled(boolean directCurrencyConversionEnabled) {
        this.directCurrencyConversionEnabled = directCurrencyConversionEnabled;
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
