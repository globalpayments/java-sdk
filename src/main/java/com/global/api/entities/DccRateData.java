package com.global.api.entities;

import com.global.api.entities.enums.DccProcessor;
import com.global.api.entities.enums.DccRateType;
import org.joda.time.DateTime;
import java.math.BigDecimal;

public class DccRateData {
    private BigDecimal cardHolderAmount;
    private String cardHolderCurrency;
    private String cardHolderRate;
    private String commissionPercentage;
    private DccProcessor dccProcessor;
    private DccRateType dccRateType;
    private String exchangeRateSourceName;
    private DateTime exchangeRateSourceTimestamp;
    private BigDecimal merchantAmount;
    private String merchantCurrency;
    private String marginRatePercentage;

    public BigDecimal getCardHolderAmount() {
        return cardHolderAmount;
    }
    public void setCardHolderAmount(BigDecimal cardHolderAmount) {
        this.cardHolderAmount = cardHolderAmount;
    }
    public String getCardHolderCurrency() {
        return cardHolderCurrency;
    }
    public void setCardHolderCurrency(String cardHolderCurrency) {
        this.cardHolderCurrency = cardHolderCurrency;
    }
    public String getCardHolderRate() {
        return cardHolderRate;
    }
    public void setCardHolderRate(String cardHolderRate) {
        this.cardHolderRate = cardHolderRate;
    }
    public String getCommissionPercentage() {
        return commissionPercentage;
    }
    public void setCommissionPercentage(String commissionPercentage) {
        this.commissionPercentage = commissionPercentage;
    }
    public DccProcessor getDccProcessor() {
        return dccProcessor;
    }
    public void setDccProcessor(DccProcessor dccProcessor) {
        this.dccProcessor = dccProcessor;
    }
    public DccRateType getDccRateType() {
        return dccRateType;
    }
    public void setDccRateType(DccRateType dccRateType) {
        this.dccRateType = dccRateType;
    }
    public String getExchangeRateSourceName() {
        return exchangeRateSourceName;
    }
    public void setExchangeRateSourceName(String exchangeRateSourceName) {
        this.exchangeRateSourceName = exchangeRateSourceName;
    }
    public DateTime getExchangeRateSourceTimestamp() {
        return exchangeRateSourceTimestamp;
    }
    public void setExchangeRateSourceTimestamp(DateTime exchangeRateSourceTimestamp) {
        this.exchangeRateSourceTimestamp = exchangeRateSourceTimestamp;
    }
    public BigDecimal getMerchantAmount() {
        return merchantAmount;
    }
    public void setMerchantAmount(BigDecimal merchantAmount) {
        this.merchantAmount = merchantAmount;
    }
    public String getMerchantCurrency() {
        return merchantCurrency;
    }
    public void setMerchantCurrency(String merchantCurrency) {
        this.merchantCurrency = merchantCurrency;
    }
    public String getMarginRatePercentage() {
        return marginRatePercentage;
    }
    public void setMarginRatePercentage(String marginRatePercentage) {
        this.marginRatePercentage = marginRatePercentage;
    }
}
