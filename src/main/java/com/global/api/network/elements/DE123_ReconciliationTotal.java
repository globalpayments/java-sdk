package com.global.api.network.elements;

import com.global.api.network.enums.DE123_TransactionType;
import com.global.api.network.enums.DE123_TotalType;

import java.math.BigDecimal;

public class DE123_ReconciliationTotal {
    private DE123_TransactionType transactionType;
    private DE123_TotalType totalType = DE123_TotalType.AmountGoodsAndServices;
    private String cardType = "    ";
    private int transactionCount;
    private BigDecimal totalAmount;

    public DE123_TransactionType getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(DE123_TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    public DE123_TotalType getTotalType() {
        return totalType;
    }
    public void setTotalType(DE123_TotalType totalType) {
        this.totalType = totalType;
    }
    public String getCardType() {
        return cardType;
    }
    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    public int getTransactionCount() {
        return transactionCount;
    }
    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
    public BigDecimal getTotalAmount() {
        if(totalAmount != null) {
            return totalAmount;
        }
        return BigDecimal.ZERO;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
