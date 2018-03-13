package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IResponseSubGroup;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

import java.math.BigDecimal;

public class AmountResponse implements IResponseSubGroup {
    private BigDecimal approvedAmount;
    private BigDecimal amountDue;
    private BigDecimal tipAmount;
    private BigDecimal cashBackAmount;
    private BigDecimal merchantFee;
    private BigDecimal taxAmount;
    private BigDecimal balance1;
    private BigDecimal balance2;

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }
    public BigDecimal getAmountDue() {
        return amountDue;
    }
    public BigDecimal getTipAmount() {
        return tipAmount;
    }
    public BigDecimal getCashBackAmount() {
        return cashBackAmount;
    }
    public BigDecimal getMerchantFee() {
        return merchantFee;
    }
    public BigDecimal getTaxAmount() {
        return taxAmount;
    }
    public BigDecimal getBalance1() {
        return balance1;
    }
    public BigDecimal getBalance2() {
        return balance2;
    }

    public AmountResponse(MessageReader mr) {
        String values = mr.readToCode(ControlCodes.FS);
        if(StringUtils.isNullOrEmpty(values))
            return;

        String[] data = values.split("\\[US\\]");
        try{
            approvedAmount = StringUtils.toAmount(data[0]);
            amountDue = StringUtils.toAmount(data[1]);
            tipAmount = StringUtils.toAmount(data[2]);
            cashBackAmount = StringUtils.toAmount(data[3]);
            merchantFee = StringUtils.toAmount(data[4]);
            taxAmount = StringUtils.toAmount(data[5]);
            balance1 = StringUtils.toAmount(data[6]);
            balance2 = StringUtils.toAmount(data[7]);
        }
        catch(IndexOutOfBoundsException e){
            // Eating this
        }
    }
}