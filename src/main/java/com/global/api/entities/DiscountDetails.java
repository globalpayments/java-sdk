package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DiscountDetails {
    private String discountName ;
    private BigDecimal discountAmount ;
    private BigDecimal discountPercentage ;
    private String discountType;
    private Integer discountPriority;
    private Boolean discountIsStackable ;
}
