package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.utils.StringUtils;

public class AmountRequest implements IRequestSubGroup {
    private String transactionAmount;
    private String tipAmount;
    private String cashBackAmount;
    private String merchantFee;
    private String taxAmount;
    private String fuelAmount;

    public String getTransactionAmount() {
        return transactionAmount;
    }
    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }
    public String getTipAmount() {
        return tipAmount;
    }
    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }
    public String getCashBackAmount() {
        return cashBackAmount;
    }
    public void setCashBackAmount(String cashBackAmount) {
        this.cashBackAmount = cashBackAmount;
    }
    public String getMerchantFee() {
        return merchantFee;
    }
    public void setMerchantFee(String merchantFee) {
        this.merchantFee = merchantFee;
    }
    public String getTaxAmount() {
        return taxAmount;
    }
    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }
    public String getFuelAmount() {
        return fuelAmount;
    }
    public void setFuelAmount(String fuelAmount) {
        this.fuelAmount = fuelAmount;
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();
        sb.append(transactionAmount);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(tipAmount);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(cashBackAmount);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(merchantFee);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(taxAmount);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(fuelAmount);

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}