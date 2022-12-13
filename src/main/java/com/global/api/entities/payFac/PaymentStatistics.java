package com.global.api.entities.payFac;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class PaymentStatistics {
    // The total monthly sales of the merchant
    private BigDecimal totalMonthlySalesAmount;
    // The total monthly sales of the merchant
    private BigDecimal averageTicketSalesAmount;
    // The merchants highest ticket amount
    private BigDecimal highestTicketSalesAmount;
}