package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class AdditionalTaxDetails {
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private String taxType;
}
