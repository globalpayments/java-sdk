package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;

public class DE30_OriginalAmounts implements IDataElement<DE30_OriginalAmounts> {
    private BigDecimal originalTransactionAmount;
    private BigDecimal originalReconciliationAmount;

    public BigDecimal getOriginalTransactionAmount() {
        return originalTransactionAmount;
    }
    public void setOriginalTransactionAmount(BigDecimal originalTransactionAmount) {
        this.originalTransactionAmount = originalTransactionAmount;
    }
    public BigDecimal getOriginalReconciliationAmount() {
        return originalReconciliationAmount;
    }
    public void setOriginalReconciliationAmount(BigDecimal originalReconciliationAmount) {
        this.originalReconciliationAmount = originalReconciliationAmount;
    }

    public DE30_OriginalAmounts fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        originalTransactionAmount = StringUtils.toAmount(sp.readString(12));
        originalReconciliationAmount = StringUtils.toAmount(sp.readString(12));

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = StringUtils.padLeft(StringUtils.toNumeric(originalTransactionAmount), 12, '0')
                .concat(StringUtils.padLeft(StringUtils.toNumeric(originalReconciliationAmount), 12, '0'));
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
