package com.global.api.terminals.upa.subgroups;

import java.math.BigDecimal;

import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.utils.JsonDoc;

public class RequestTransactionFields {
    private String baseAmount;
    private BigDecimal taxAmount;
    private BigDecimal tipAmount;
    private Integer taxIndicator;
    private BigDecimal cashBackAmount;
    private Integer invoiceNbr;
    private String totalAmount;
    private String terminalRefNumber;

    public void setParams(TerminalManageBuilder builder) {
        if (builder.getTerminalRefNumber() != null) {
            this.terminalRefNumber = builder.getTerminalRefNumber();
        }

        if (builder.getGratuity() != null) {
            this.tipAmount = builder.getGratuity();
        }
    }

    public void setParams(TerminalAuthBuilder builder) {
        if (builder.getAmount() != null) {
            if (builder.getTransactionType() == TransactionType.Refund) {
                this.totalAmount = builder.getAmount().toString();
            } else {
                this.baseAmount = builder.getAmount().toString();
            }
        }

        if (builder.getGratuity() != null) {
            this.tipAmount = builder.getGratuity();
        }

        if (builder.getCashBackAmount() != null) {
            this.cashBackAmount = builder.getCashBackAmount();
        }

        if (builder.getTaxAmount() != null) {
            this.taxAmount = builder.getTaxAmount();
        }

        if (builder.getInvoiceNumber() != null) {
            this.invoiceNbr = Integer.parseInt(builder.getInvoiceNumber());
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
        boolean hasContents = false;

        if (terminalRefNumber != null) {
            params.set("tranNo", terminalRefNumber);
            hasContents = true;
        }

        if (totalAmount != null) {
            params.set("totalAmount", totalAmount);
            hasContents = true;
        }

        if (baseAmount != null) {
            params.set("baseAmount", baseAmount);
            hasContents = true;
        }

        if (taxAmount != null) {
            params.set("taxAmount", taxAmount.toString());
            hasContents = true;
        }

        if (tipAmount != null) {
            params.set("tipAmount", tipAmount.toString());
            hasContents = true;
        }

        if (taxIndicator != null) {
            params.set("taxIndicator", taxIndicator);
            hasContents = true;
        }

        if (cashBackAmount != null) {
            params.set("cashBackAmount", cashBackAmount.toString());
            hasContents = true;
        }

        if (invoiceNbr != null) {
            params.set("invoiceNbr", invoiceNbr);
            hasContents = true;
        }

        return hasContents ? params : null;
    }
}
