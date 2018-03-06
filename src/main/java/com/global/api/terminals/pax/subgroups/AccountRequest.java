package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.utils.StringUtils;

public class AccountRequest implements IRequestSubGroup {
    private String accountNumber;
    private String expd;
    private String cvvCode;
    private String ebtType;
    private String voucherNumber;
    private String dupOverrideFlag;

    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getExpd() {
        return expd;
    }
    public void setExpd(String expd) {
        this.expd = expd;
    }
    public String getCvvCode() {
        return cvvCode;
    }
    public void setCvvCode(String cvvCode) {
        this.cvvCode = cvvCode;
    }
    public String getEbtType() {
        return ebtType;
    }
    public void setEbtType(String ebtType) {
        this.ebtType = ebtType;
    }
    public String getVoucherNumber() {
        return voucherNumber;
    }
    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }
    public String getDupOverrideFlag() {
        return dupOverrideFlag;
    }
    public void setDupOverrideFlag(String dupOverrideFlag) {
        this.dupOverrideFlag = dupOverrideFlag;
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();
        sb.append(accountNumber);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(expd);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(cvvCode);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(ebtType);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(voucherNumber);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(dupOverrideFlag);

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}