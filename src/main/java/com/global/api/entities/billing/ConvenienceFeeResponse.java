package com.global.api.entities.billing;

import java.math.BigDecimal;

public class ConvenienceFeeResponse extends BillingResponse {
    protected BigDecimal convenienceFee;

    public BigDecimal getConvenienceFee() {
        return convenienceFee;
    }

    public void setConvenienceFee(BigDecimal convenienceFee) {
        this.convenienceFee = convenienceFee;
    }
}
