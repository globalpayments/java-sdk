package com.global.api.entities;

import com.global.api.entities.enums.TaxCategory;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdditionalTaxDetails {
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private String taxType;
    private TaxCategory taxCategory;
}
