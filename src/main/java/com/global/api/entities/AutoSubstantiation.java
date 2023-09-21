package com.global.api.entities;

import java.math.BigDecimal;
import java.util.HashMap;

public class AutoSubstantiation {
    private HashMap<String, BigDecimal> amounts;
    private String merchantVerificationValue;
    private boolean realTimeSubstantiation;

    public HashMap<String, BigDecimal> getAmounts() { return amounts; }
    public String getMerchantVerificationValue() {
        return merchantVerificationValue;
    }
    public void setMerchantVerificationValue(String merchantVerificationValue) {
        this.merchantVerificationValue = merchantVerificationValue;
    }
    public boolean isRealTimeSubstantiation() {
        return realTimeSubstantiation;
    }
    public void setRealTimeSubstantiation(boolean realTimeSubstantiation) {
        this.realTimeSubstantiation = realTimeSubstantiation;
    }

    // AMOUNTS
    public BigDecimal getClinicSubTotal() {
        return amounts.get("SUBTOTAL_CLINIC_OR_OTHER_AMT");
    }
    public void setClinicSubTotal(BigDecimal value) {
        amounts.put("SUBTOTAL_CLINIC_OR_OTHER_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }
    public BigDecimal getDentalSubTotal() {
        return amounts.get("SUBTOTAL_DENTAL_AMT");
    }
    public void setDentalSubTotal(BigDecimal value) {
        amounts.put("SUBTOTAL_DENTAL_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }
    public BigDecimal getPrescriptionSubTotal() {
        return amounts.get("SUBTOTAL_PRESCRIPTION_AMT");
    }
    public void setPrescriptionSubTotal(BigDecimal value) {
        amounts.put("SUBTOTAL_PRESCRIPTION_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }
    public BigDecimal getTotalHelthcareAmount() {
        return amounts.get("TOTAL_HEALTHCARE_AMT");
    }
    public BigDecimal getVisionSubTotal() {
        return amounts.get("SUBTOTAL_VISION__OPTICAL_AMT");
    }
    public void setVisionSubTotal(BigDecimal value) {
        amounts.put("SUBTOTAL_VISION__OPTICAL_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }

    public BigDecimal getCopaySubTotal() {
        return amounts.get("SUBTOTAL_COPAY_AMT");
    }
    public void setCopaySubTotal(BigDecimal value) {
        amounts.put("SUBTOTAL_COPAY_AMT", value);
        amounts.put("TOTAL_HEALTHCARE_AMT", amounts.get("TOTAL_HEALTHCARE_AMT").add(value));
    }

    public AutoSubstantiation() {
        amounts = new HashMap<String, BigDecimal>();
        amounts.put("TOTAL_HEALTHCARE_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_PRESCRIPTION_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_VISION__OPTICAL_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_CLINIC_OR_OTHER_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_DENTAL_AMT", new BigDecimal("0"));
        amounts.put("SUBTOTAL_COPAY_AMT", new BigDecimal("0"));
    }
}
