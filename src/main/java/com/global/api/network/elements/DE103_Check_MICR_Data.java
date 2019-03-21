package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class DE103_Check_MICR_Data implements IDataElement<DE103_Check_MICR_Data> {
    private String transitNumber;
    private String accountNumber;
    private String sequenceNumber;

    public String getTransitNumber() {
        return transitNumber;
    }
    public void setTransitNumber(String transitNumber) {
        this.transitNumber = transitNumber;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public DE103_Check_MICR_Data fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        transitNumber = sp.readToChar('\\');
        accountNumber = sp.readToChar('\\');
        sequenceNumber = sp.readRemaining();

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = transitNumber;

        if(!StringUtils.isNullOrEmpty(accountNumber)) {
            rvalue = rvalue.concat("\\" + accountNumber);
        }

        if(!StringUtils.isNullOrEmpty(sequenceNumber)) {
            rvalue = rvalue.concat("\\" + sequenceNumber);
        }

        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
