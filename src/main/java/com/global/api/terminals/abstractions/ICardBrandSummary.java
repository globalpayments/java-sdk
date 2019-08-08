package com.global.api.terminals.abstractions;

import com.global.api.entities.enums.CardType;

import java.math.BigDecimal;

public interface ICardBrandSummary {
    CardType getCardType();
    int getCreditCount();
    BigDecimal getCreditAmount();
    int getDebitCount();
    BigDecimal getDebitAmount();
    int getSaleCount();
    BigDecimal getSaleAmount();
    int getRefundCount();
    BigDecimal getRefundAmount();
    int getTotalCount();
    BigDecimal getTotalAmount();
}
