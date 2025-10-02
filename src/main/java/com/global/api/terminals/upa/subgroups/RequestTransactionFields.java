package com.global.api.terminals.upa.subgroups;

import java.math.BigDecimal;

import com.global.api.entities.AutoSubstantiation;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;
import com.global.api.terminals.builders.TerminalAuthBuilder;
import com.global.api.terminals.builders.TerminalManageBuilder;
import com.global.api.utils.JsonDoc;
import com.global.api.utils.StringUtils;

public class RequestTransactionFields {
    private String amount;
    private AutoSubstantiation autoSubstantiation;
    private String baseAmount;
    private String cashBackAmount;
    private boolean commercialRequest;
    private String referenceNumber;
    private String giftTransactionType;
    private String invoiceNbr;
    private String preAuthAmount;
    private String surchargeAmount;
    private String taxAmount;
    private String taxIndicator;
    private String tipAmount;
    private String terminalRefNumber;
    private String totalAmount;
    private String purchaseOrder;
    private String allowDuplicate;
    private String confirmAmount;

    private static final String PREAUTH_AMOUNT = "preAuthAmount";
    private static final String REFERENCE_REQUIRED = "Reference number is required";

    public void setParams(TerminalManageBuilder builder) {
        if(builder.getTransactionType().equals(TransactionType.DeleteOpenTab)){
            getDeletePreAuthRequestParam(builder);
            return;
        }
        if(builder.getTransactionType().equals(TransactionType.Void)){
            getVoidRequestParam(builder);
            return;
        }
        if (builder.getTerminalRefNumber() != null) {
            this.terminalRefNumber = builder.getTerminalRefNumber();
        }

        if (builder.getGratuity() != null) {
            this.tipAmount = StringUtils.toCurrencyString(builder.getGratuity());
        }

        if (builder.getTransactionId() != null && builder.getTransactionType().equals(TransactionType.Capture)) {
            this.referenceNumber = builder.getTransactionId();
        }

        if (builder.getAmount() != null) {
            this.amount = builder.getAmount().toString();
        }
        if (builder.getPreAuthAmount() != null) {
            this.preAuthAmount = StringUtils.toCurrencyString(builder.getPreAuthAmount());
        }
        if (builder.getPurchaseOrder() != null) {
            this.purchaseOrder = builder.getPurchaseOrder();
        }
        if (builder.getTaxIndicator() != null) {
            this.taxIndicator = builder.getTaxIndicator().toString();
        }
        if (builder.getTaxAmount() != null) {
            this.taxAmount = builder.getTaxAmount().toString();
        }

    }

    public void setParams(TerminalAuthBuilder builder) {
        if (builder.getAmount() != null && builder.getTransactionType() == TransactionType.Refund) {
            this.baseAmount = StringUtils.toCurrencyString(builder.getAmount());
            this.totalAmount = StringUtils.toCurrencyString(builder.getTotalAmount());
        } else if (builder.getAmount() != null && builder.getTransactionType() == TransactionType.Activate) {
            this.totalAmount = StringUtils.toCurrencyString(builder.getTotalAmount());
        } else if (builder.getTransactionType() == TransactionType.Auth) {
            this.amount = StringUtils.toCurrencyString(builder.getAmount());
        } else {
            this.baseAmount = StringUtils.toCurrencyString(builder.getAmount());
        }

        if (builder.getAutoSubstantiation() != null) {
            this.autoSubstantiation = builder.getAutoSubstantiation();
        }

        if (builder.getGratuity() != null) {
            this.tipAmount = StringUtils.toCurrencyString(builder.getGratuity());
        }

        if (builder.getCashBackAmount() != null) {
            this.cashBackAmount = StringUtils.toCurrencyString(builder.getCashBackAmount());
        }

        if(builder.getSurchargeAmount() != null) {
            this.surchargeAmount = StringUtils.toCurrencyString(builder.getSurchargeAmount());
        }

        if (builder.getTaxAmount() != null) {
            this.taxAmount = StringUtils.toCurrencyString(builder.getTaxAmount());
        }

        if (builder.getInvoiceNumber() != null) {
            this.invoiceNbr = builder.getInvoiceNumber();
        }

        if (builder.getTerminalRefNumber() != null) {
            this.terminalRefNumber = builder.getTerminalRefNumber();
        }

        if (builder.getTransactionId() != null) {
            this.referenceNumber = builder.getTransactionId();
        }

        if (builder.isCommercialRequest()) {
            this.commercialRequest = true;
        }

        if (builder.getGiftTransactionType() != null) {
            this.giftTransactionType = builder.getGiftTransactionType().name();
        }

        if (builder.getPreAuthAmount() != null) {
            this.preAuthAmount = builder.getPreAuthAmount().toString();
        }

        if (builder.getTaxIndicator() != null) {
            this.taxIndicator = builder.getTaxIndicator().toString();
        }
        if (!builder.getTransactionType().equals(TransactionType.Activate) && builder.isAllowDuplicates()) {

            if (builder.isAllowDuplicates()) {
                this.allowDuplicate = "1";
            } else {
                this.allowDuplicate = "0";
            }
        }

        if (builder.getConfirmAmount() != null) {
            this.confirmAmount = builder.getConfirmAmount();
        }
    }

    public JsonDoc getElementsJson() {
        JsonDoc params = new JsonDoc();
        boolean hasContents = false;


        if (amount != null) {
            params.set("amount", amount);
            hasContents = true;
        }

        if (referenceNumber != null) {
            params.set("referenceNumber", referenceNumber);
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
            params.set("taxAmount", taxAmount);
            hasContents = true;
        }

        if (tipAmount != null) {
            params.set("tipAmount", tipAmount);
            hasContents = true;
        }

        if (taxIndicator != null) {
            params.set("taxIndicator", taxIndicator);
            hasContents = true;
        }

        if (cashBackAmount != null) {
            params.set("cashBackAmount", cashBackAmount);
            hasContents = true;
        }

        if (invoiceNbr != null) {
            params.set("invoiceNbr", invoiceNbr);
            hasContents = true;
        }

        if(surchargeAmount != null) {
            params.set("surcharge", invoiceNbr);
            hasContents = true;
        }

        if (terminalRefNumber != null) {
            params.set("tranNo", terminalRefNumber);
            hasContents = true;
        }
        if (purchaseOrder != null) {
            params.set("purchaseOrder", purchaseOrder);
            hasContents = true;
        }

        if (allowDuplicate != null) {
            params.set("allowDuplicate", allowDuplicate);
            hasContents = true;
        }

        if (confirmAmount != null) {
            params.set("confirmAmount", confirmAmount);
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
        if (builder.getTransactionId() == null) {
            throw new IllegalArgumentException(REFERENCE_REQUIRED);
        }
            this.referenceNumber = builder.getTransactionId();

            if (builder.getPreAuthAmount() != null) {
                this.preAuthAmount = builder.getPreAuthAmount().toString();
        }
    }

    private void getVoidRequestParam(TerminalManageBuilder builder) {
        if (builder.getTerminalRefNumber() != null) {
            this.terminalRefNumber = builder.getTerminalRefNumber();
        }
        if (builder.getTransactionId() != null) {
            this.referenceNumber = builder.getTransactionId();
        }
    }
}
