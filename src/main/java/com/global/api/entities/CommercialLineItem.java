package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CommercialLineItem {
    private String alternateTaxId ;
    private String commodityCode ;
    private String description ;
    private BigDecimal extendedAmount;
    private CreditDebitIndicator creditDebitIndicator;
    private NetGrossIndicator netGrossIndicator;
    private String name ;
    private String productCode ;
    private BigDecimal quantity ;
    private String unitOfMeasure ;
    private BigDecimal unitCost ;
    private BigDecimal taxAmount ;
    private BigDecimal taxName ;
    private String upc;
    private BigDecimal taxPercentage;
    private DiscountDetails discountDetails ;
    private BigDecimal totalAmount;
}
