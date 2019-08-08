package com.global.api.terminals.hpa.responses;

import com.global.api.entities.enums.CardType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.ICardBrandSummary;
import com.global.api.utils.Element;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;

public class CardBrandSummary extends SipBaseResponse implements ICardBrandSummary {
    private CardType cardType;
    private int creditCount;
    private BigDecimal creditAmount;
    private int debitCount;
    private BigDecimal debitAmount;
    private int saleCount;
    private BigDecimal saleAmount;
    private int refundCount;
    private BigDecimal refundAmount;
    private int totalCount;
    private BigDecimal totalAmount;

    public CardType getCardType() {
        return cardType;
    }
    public int getCreditCount() {
        return creditCount;
    }
    public BigDecimal getCreditAmount() {
        return creditAmount;
    }
    public int getDebitCount() {
        return debitCount;
    }
    public BigDecimal getDebitAmount() {
        return debitAmount;
    }
    public int getSaleCount() {
        return saleCount;
    }
    public BigDecimal getSaleAmount() {
        return saleAmount;
    }
    public int getRefundCount() {
        return refundCount;
    }
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    public int getTotalCount() {
        return totalCount;
    }
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public CardBrandSummary(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }

    protected void mapResponse(Element response) {
        Element[] fields = response.getAll("Field");
        for(int i = 0; i < fields.length; i++) {
            Element field = fields[i];

            String key = field.getString("Key");
            String value = field.getString("Value");

            if(key.equalsIgnoreCase("CardType")) {
                cardType = mapCardType(value);
            }
            else if(key.equalsIgnoreCase("TransType")) {
                int count = fields[++i].getInt("Value");
                BigDecimal amount = StringUtils.toAmount(fields[++i].getString("Value"));

                if(value.equalsIgnoreCase("CREDIT")) {
                    creditCount = count;
                    creditAmount = amount;
                }
                else if(value.equalsIgnoreCase("DEBIT")) {
                    debitCount = count;
                    debitAmount = amount;
                }
                else if(value.equalsIgnoreCase("SALE")) {
                    saleCount = count;
                    saleAmount = amount;
                }
                else if(value.equalsIgnoreCase("REFUND")) {
                    refundCount = count;
                    refundAmount = amount;
                }
                else if(value.equalsIgnoreCase("TOTAL")) {
                    totalCount = count;
                    totalAmount = amount;
                }
            }
        }
    }

    private CardType mapCardType(String value) {
        if(value.equalsIgnoreCase("VISA")){
            return CardType.VISA;
        }
        else if(value.equalsIgnoreCase("MASTERCARD")) {
            return CardType.MC;
        }
        else if(value.equalsIgnoreCase("AMERICAN EXPRESS")) {
            return CardType.AMEX;
        }
        else if(value.equalsIgnoreCase("DISCOVER")) {
            return CardType.DISC;
        }
        else {
            return CardType.PAYPALECOMMERCE;
        }
    }
}
