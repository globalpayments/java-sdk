package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class OrderDetails {
    public BigDecimal insuranceAmount;
    @Accessors(fluent = true)
    public boolean hasInsurance;
    public BigDecimal handlingAmount;
    public String description;

    private String localTaxPercentage;
    private String buyerRecipientName;
    private String stateTaxIdReference;
    private String merchantTaxIdReference;
    private List<OrderDetails.Tax> taxes;

    @Getter
    @Setter
    public static class Tax {
        private String type;
        private String amount;

        public Tax(String type, String amount) {
            this.type = type;
            this.amount = amount;
        }
    }
}