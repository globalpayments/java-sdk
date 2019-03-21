package com.global.api.network.elements;

import com.global.api.network.enums.DE3_AccountType;
import com.global.api.network.enums.DE54_AmountTypeCode;
import com.global.api.network.enums.Iso4217_CurrencyCode;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;

public class DE54_AdditionalAmount {
    private DE3_AccountType accountType;
    private DE54_AmountTypeCode amountType;
    private Iso4217_CurrencyCode currencyCode;
    private BigDecimal amount;

    public DE3_AccountType getAccountType() {
        return accountType;
    }
    public void setAccountType(DE3_AccountType accountType) {
        this.accountType = accountType;
    }
    public DE54_AmountTypeCode getAmountType() {
        return amountType;
    }
    public void setAmountType(DE54_AmountTypeCode amountType) {
        this.amountType = amountType;
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

    public DE54_AdditionalAmount fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        accountType = sp.readStringConstant(2, DE3_AccountType.class);
        amountType = sp.readStringConstant(2, DE54_AmountTypeCode.class);
        currencyCode = sp.readStringConstant(3, Iso4217_CurrencyCode.class);
        sp.readString(1); // I don't know what this is
        amount = StringUtils.toAmount(sp.readString(12));

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = accountType.getValue()
                .concat(amountType.getValue())
                .concat(currencyCode.getValue())
                .concat("D")
                .concat(StringUtils.toNumeric(amount, 12));
        return rvalue.getBytes();
    }
}
