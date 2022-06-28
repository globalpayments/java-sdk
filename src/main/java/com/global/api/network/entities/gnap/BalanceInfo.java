package com.global.api.network.entities.gnap;

import com.global.api.network.enums.gnap.AmountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class BalanceInfo {
    private String accountType1;
    private AmountType amountType1;
    private String amountSign1;
    private String currencyCode1;
    private BigDecimal amount1;
    private String accountType2;
    private AmountType amountType2;
    private String amountSign2;
    private String currencyCode2;
    private BigDecimal amount2;
}
