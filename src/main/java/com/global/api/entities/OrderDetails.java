package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class OrderDetails {
    public BigDecimal insuranceAmount;
    @Accessors(fluent = true)
    public boolean hasInsurance;
    public BigDecimal handlingAmount;
    public String description;
}