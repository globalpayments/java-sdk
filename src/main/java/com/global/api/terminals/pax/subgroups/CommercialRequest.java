package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.utils.StringUtils;

public class CommercialRequest implements IRequestSubGroup {
    private String poNumber;
    private String customerCode;
    private String taxExempt;
    private String taxExemptId;

    public String getPoNumber() {
        return poNumber;
    }
    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }
    public String getCustomerCode() {
        return customerCode;
    }
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }
    public String getTaxExempt() {
        return taxExempt;
    }
    public void setTaxExempt(String taxExempt) {
        this.taxExempt = taxExempt;
    }
    public String getTaxExemptId() {
        return taxExemptId;
    }
    public void setTaxExemptId(String taxExemptId) {
        this.taxExemptId = taxExemptId;
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();
        sb.append(poNumber);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(customerCode);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(taxExempt);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(taxExemptId);

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}