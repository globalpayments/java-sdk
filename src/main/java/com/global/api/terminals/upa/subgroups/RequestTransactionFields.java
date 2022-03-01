package com.global.api.terminals.upa.subgroups;

import java.math.BigDecimal;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.utils.JsonDoc;

public class RequestTransactionFields {
    private String amount;
    private AutoSubstantiation autoSubstantiation;
    private String baseAmount;
    private boolean commercialRequest;
    private BigDecimal taxAmount;
    private BigDecimal tipAmount;
    private Integer taxIndicator;
    private BigDecimal cashBackAmount;
    private Integer invoiceNbr;
    private String totalAmount;
    private String terminalRefNumber;
    private String gatewayRefNumber;

    public void setParams(TerminalManageBuilder builder) {
        if (builder.getTerminalRefNumber() != null) {
            this.terminalRefNumber = builder.getTerminalRefNumber();
        }

        if (builder.getGratuity() != null) {
            this.tipAmount = builder.getGratuity();
        }

        if (builder.getTransactionId() != null) {
            this.gatewayRefNumber = builder.getTransactionId();
        }

        if (builder.getAmount() != null) {
            this.amount = builder.getAmount().toString();
        }
    }

    public void setParams(TerminalAuthBuilder builder) {
        if (builder.getAmount() != null) {
            if (builder.getTransactionType() == TransactionType.Refund) {
                this.totalAmount = builder.getAmount().toString();
            } else if (builder.getTransactionType() == TransactionType.Auth) {
                this.amount = builder.getAmount().toString();
            } else {
                this.baseAmount = builder.getAmount().toString();
            }
        }

        if (builder.getAutoSubstantiation() != null) {
            this.autoSubstantiation = builder.getAutoSubstantiation();
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

        if (builder.getReferenceNumber() != null) {
            this.gatewayRefNumber = builder.getReferenceNumber();
        }

        if (builder.getTransactionId() != null) {
            this.gatewayRefNumber = builder.getTransactionId();
        }

        if (builder.getCommercialRequest()) {
            this.commercialRequest = builder.getCommercialRequest();
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
        boolean hasContents = false;

        if (amount != null) {
            params.set("amount", amount);
            hasContents = true;
        }

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

        if (gatewayRefNumber != null) {
            params.set("referenceNumber", gatewayRefNumber);
            hasContents = true;
        }

        if (autoSubstantiation != null) {
            params.set("cardIsHSAFSA", "1");
            hasContents = true;

            if (!autoSubstantiation.getPrescriptionSubTotal().equals(new BigDecimal("0"))) {
                params.set("prescriptionAmount", autoSubstantiation.getPrescriptionSubTotal().toString());
            }

            if (!autoSubstantiation.getVisionSubTotal().equals(new BigDecimal("0"))) {
                params.set("visionOpticalAmount", autoSubstantiation.getVisionSubTotal().toString());
            }

            if (!autoSubstantiation.getDentalSubTotal().equals(new BigDecimal("0"))) {
                params.set("dentalAmount", autoSubstantiation.getDentalSubTotal().toString());
            }

            if (!autoSubstantiation.getClinicSubTotal().equals(new BigDecimal("0"))) {
                params.set("clinicAmount", autoSubstantiation.getClinicSubTotal().toString());
            }
        }

        if (commercialRequest) {
            params.set("processCPC", "1");
        }

        return hasContents ? params : null;
    }
}
