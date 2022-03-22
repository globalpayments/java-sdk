package com.global.api.terminals.upa.responses;

import com.global.api.entities.enums.CardType;
import com.global.api.terminals.abstractions.ICardBrandSummary;
import com.global.api.utils.JsonDoc;

import java.math.BigDecimal;

public class UpaCardBrandSummary implements ICardBrandSummary {
    private final BigDecimal gratuityAmount;
    private final BigDecimal returnAmount;
    private final Integer returnCount;
    private final BigDecimal saleAmount;
    private final Integer saleCount;
    private final Integer transactionCount;
    private final BigDecimal totalAmount;

    public UpaCardBrandSummary(JsonDoc record) {
        gratuityAmount = record.getDecimal("totalGratuityAmt");
        returnAmount = record.getDecimal("returnAmt");
        returnCount = record.getInt("returnCnt");
        saleAmount = record.getDecimal("saleAmt");
        saleCount = record.getInt("saleCnt");
        transactionCount = record.getInt("totalCnt");
        totalAmount = record.getDecimal("totalAmount");
    }

    public int getSaleCount() {
        return saleCount;
    }

    public BigDecimal getSaleAmount() {
        return saleAmount;
    }

    public int getRefundCount() {
        return returnCount;
    }

    public BigDecimal getRefundAmount() {
        return returnAmount;
    }

    public int getTotalCount() {
        return transactionCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public CardType getCardType() {
        return null;
    }

    public int getCreditCount() {
        return 0;
    }

    public BigDecimal getCreditAmount() {
        return null;
    }

    public int getDebitCount() {
        return 0;
    }

    public BigDecimal getDebitAmount() {
        return null;
    }

    public BigDecimal getGratuityAmount() {
        return gratuityAmount;
    }
}
