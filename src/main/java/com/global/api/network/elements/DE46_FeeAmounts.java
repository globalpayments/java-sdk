package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;

public class DE46_FeeAmounts implements IDataElement<DE46_FeeAmounts> {
    private FeeType feeTypeCode;
    private Iso4217_CurrencyCode currencyCode;
    private BigDecimal amount;
    private BigDecimal conversionRate;
    private BigDecimal reconciliationAmount;
    private Iso4217_CurrencyCode reconciliationCurrencyCode;

    public FeeType getFeeTypeCode() {
        return feeTypeCode;
    }
    public void setFeeTypeCode(FeeType feeTypeCode) {
        this.feeTypeCode = feeTypeCode;
    }
    public Iso4217_CurrencyCode getCurrencyCode() {
        return currencyCode;
    }
    public void setCurrencyCode(Iso4217_CurrencyCode currencyCode) {
        this.currencyCode = currencyCode;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public BigDecimal getConversionRate() {
        return conversionRate;
    }
    public void setConversionRate(BigDecimal conversionRate) {
        this.conversionRate = conversionRate;
    }
    public BigDecimal getReconciliationAmount() {
        return reconciliationAmount;
    }
    public void setReconciliationAmount(BigDecimal reconciliationAmount) {
        this.reconciliationAmount = reconciliationAmount;
    }
    public Iso4217_CurrencyCode getReconciliationCurrencyCode() {
        return reconciliationCurrencyCode;
    }
    public void setReconciliationCurrencyCode(Iso4217_CurrencyCode reconciliationCurrencyCode) {
        this.reconciliationCurrencyCode = reconciliationCurrencyCode;
    }

    public DE46_FeeAmounts fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        feeTypeCode = sp.readStringConstant(2, FeeType.class);
        currencyCode = sp.readStringConstant(3, Iso4217_CurrencyCode.class);
        String D1 = sp.readString(1); // TODO: We don't know what this is
        amount = StringUtils.toAmount(sp.readString(8));
        conversionRate = new BigDecimal(sp.readString(8));
        String D2 = sp.readString(1); // TODO: We don't know what this is
        reconciliationAmount = StringUtils.toAmount(sp.readString(8));
        reconciliationCurrencyCode =  sp.readStringConstant(3, Iso4217_CurrencyCode.class);

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = feeTypeCode.getValue()
                .concat(currencyCode.getValue())
                .concat("D")
                .concat(StringUtils.toNumeric(amount, 8))
                .concat(StringUtils.toNumeric(conversionRate, 8))
                .concat("D")
                .concat(StringUtils.toNumeric(reconciliationAmount, 8))
                .concat(reconciliationCurrencyCode.getValue());
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
