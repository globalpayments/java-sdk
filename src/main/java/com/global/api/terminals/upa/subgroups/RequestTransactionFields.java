package com.global.api.terminals.upa.subgroups;

import java.math.BigDecimal;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

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
    private String giftTransactionType;
    private String preAuthAmount;

    private static final String PREAUTH_AMOUNT = "preAuthAmount";
    private static final String TERMINAL_REF_REQUIRED = "Terminal reference number is required";

    public void setParams(TerminalManageBuilder builder) {
        if(builder.getTransactionType().equals(TransactionType.DeleteOpenTab)){
            getDeletePreAuthRequestParam(builder);
            return;
        }
        if (builder.getTerminalRefNumber() != null) {
            this.terminalRefNumber = builder.getTerminalRefNumber();
        }

        if (builder.getGratuity() != null) {
            this.tipAmount = builder.getGratuity();
        }

        if (builder.getTransactionId() != null && !builder.getTransactionType().equals(TransactionType.Capture)) {
            this.gatewayRefNumber = builder.getTransactionId();
        }

        if (builder.getAmount() != null) {
            this.amount = StringUtils.toCurrencyString(builder.getAmount());
        }
        if (builder.getPreAuthAmount() != null) {
            this.preAuthAmount = StringUtils.toCurrencyString(builder.getPreAuthAmount());
        }

    }

    public void setParams(TerminalAuthBuilder builder) {
        if (builder.getAmount() != null) {
            if (builder.getTransactionType() == TransactionType.Refund || builder.getTransactionType() == TransactionType.Activate) {
                this.totalAmount = builder.getAmount().toString();
            } else if (builder.getTransactionType() == TransactionType.Auth) {
                this.amount = StringUtils.toCurrencyString(builder.getAmount());
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
            this.terminalRefNumber = builder.getReferenceNumber();
        }

        if (builder.getTransactionId() != null) {
            this.gatewayRefNumber = builder.getTransactionId();
        }

        if (builder.getCommercialRequest()) {
            this.commercialRequest = builder.getCommercialRequest();
        }

        if (builder.getGiftTransactionType() != null) {
            this.giftTransactionType = builder.getGiftTransactionType().name();
        }

        if (builder.getPreAuthAmount() != null) {
            this.preAuthAmount = builder.getPreAuthAmount().toString();
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
        boolean hasContents = false;

        if (amount != null) {
            params.set("amount", amount);
            hasContents = true;
        }

        if (gatewayRefNumber != null) {
            params.set("tranNo", gatewayRefNumber);
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

        if (terminalRefNumber != null) {
            params.set("referenceNumber", terminalRefNumber);
            hasContents = true;
        }

        if (autoSubstantiation != null) {
            hasContents = true;

            if (!autoSubstantiation.getPrescriptionSubTotal().equals(new BigDecimal("0"))) {
                params.set("prescriptionAmount", StringUtils.toCurrencyString(autoSubstantiation.getPrescriptionSubTotal()));
            }

            if (!autoSubstantiation.getVisionSubTotal().equals(new BigDecimal("0"))) {
                params.set("visionOpticalAmount", StringUtils.toCurrencyString(autoSubstantiation.getVisionSubTotal()));
            }

            if (!autoSubstantiation.getDentalSubTotal().equals(new BigDecimal("0"))) {
                params.set("dentalAmount", StringUtils.toCurrencyString(autoSubstantiation.getDentalSubTotal()));
            }

            if (!autoSubstantiation.getClinicSubTotal().equals(new BigDecimal("0"))) {
                params.set("clinicAmount", StringUtils.toCurrencyString(autoSubstantiation.getClinicSubTotal()));
            }
        }

        if (commercialRequest) {
            params.set("processCPC", "1");
        }

        if (giftTransactionType != null) {
            params.set("transactionType", giftTransactionType);
        }

        if (preAuthAmount != null) {
            params.set(PREAUTH_AMOUNT, preAuthAmount);
        }

        return hasContents ? params : null;
    }
    public void getDeletePreAuthRequestParam(TerminalManageBuilder builder){
        if (builder.getTerminalRefNumber() == null) {
            throw new IllegalArgumentException(TERMINAL_REF_REQUIRED);
        }
            this.terminalRefNumber = builder.getTerminalRefNumber();

            if (builder.getPreAuthAmount() != null) {
                this.preAuthAmount = builder.getPreAuthAmount().toString();
        }
    }
}
