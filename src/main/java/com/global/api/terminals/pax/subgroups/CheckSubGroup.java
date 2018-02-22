package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

public class CheckSubGroup implements IRequestSubGroup, IResponseSubGroup {
    private String saleType;
    private String routingNumber;
    private String accountNumber;
    private String checkNumber;
    private String checkType;
    private String idType;
    private String idValue;
    private String dob;
    private String phoneNumber;
    private String zipCode;

    public String getSaleType() {
        return saleType;
    }
    public void setSaleType(String saleType) {
        this.saleType = saleType;
    }
    public String getRoutingNumber() {
        return routingNumber;
    }
    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getCheckNumber() {
        return checkNumber;
    }
    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }
    public String getCheckType() {
        return checkType;
    }
    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }
    public String getIdType() {
        return idType;
    }
    public void setIdType(String idType) {
        this.idType = idType;
    }
    public String getIdValue() {
        return idValue;
    }
    public void setIdValue(String idValue) {
        this.idValue = idValue;
    }
    public String getDob() {
        return dob;
    }
    public void setDob(String dob) {
        this.dob = dob;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public CheckSubGroup() { }
    public CheckSubGroup(MessageReader br) {
        String values = br.readToCode(ControlCodes.FS);
        if (StringUtils.isNullOrEmpty(values))
            return;

        String[] data = values.split("\\[US\\]");
        try {
            this.saleType = data[0];
            this.routingNumber = data[1];
            this.accountNumber = data[2];
            this.checkNumber = data[3];
            this.checkType = data[4];
            this.idType = data[5];
            this.idValue = data[6];
            this.dob = data[7];
            this.phoneNumber = data[8];
            this.zipCode = data[9];
        }
        catch (IndexOutOfBoundsException e) {
            // nom nom
        }
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();

        sb.append(saleType);
        sb.append(ControlCodes.US.getByte());
        sb.append(routingNumber);
        sb.append(ControlCodes.US.getByte());
        sb.append(accountNumber);
        sb.append(ControlCodes.US.getByte());
        sb.append(checkNumber);
        sb.append(ControlCodes.US.getByte());
        sb.append(checkType);
        sb.append(ControlCodes.US.getByte());
        sb.append(idType);
        sb.append(ControlCodes.US.getByte());
        sb.append(idValue);
        sb.append(ControlCodes.US.getByte());
        sb.append(dob);
        sb.append(ControlCodes.US.getByte());
        sb.append(phoneNumber);
        sb.append(ControlCodes.US.getByte());
        sb.append(zipCode);
        sb.append(ControlCodes.US.getByte());

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}