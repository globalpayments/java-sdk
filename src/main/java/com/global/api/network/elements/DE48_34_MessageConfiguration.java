package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class DE48_34_MessageConfiguration implements IDataElement<DE48_34_MessageConfiguration> {
    private Boolean performDateCheck;
    private Boolean echoSettlementData;
    private Boolean includeLoyaltyData;
    private String transactionGroupId;
    @Getter @Setter
    private Boolean incrementalSupportIndicator;

    public Boolean isPerformDateCheck() {
        return performDateCheck;
    }
    public void setPerformDateCheck(Boolean performDateCheck) {
        this.performDateCheck = performDateCheck;
    }
    public Boolean isEchoSettlementData() {
        return echoSettlementData;
    }
    public void setEchoSettlementData(Boolean echoSettlementData) {
        this.echoSettlementData = echoSettlementData;
    }
    public Boolean isIncludeLoyaltyData() {
        return includeLoyaltyData;
    }
    public void setIncludeLoyaltyData(Boolean includeLoyaltyData) {
        this.includeLoyaltyData = includeLoyaltyData;
    }
    public String getTransactionGroupId() {
        return transactionGroupId;
    }
    public void setTransactionGroupId(String transactionGroupId) {
        this.transactionGroupId = transactionGroupId;
    }
    public DE48_34_MessageConfiguration fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        performDateCheck = sp.readBoolean();
        echoSettlementData = sp.readBoolean();
        includeLoyaltyData = sp.readBoolean();
        transactionGroupId = sp.readString(6);
        incrementalSupportIndicator = sp.readBoolean();
        return this;
    }

    public byte[] toByteArray() {
        String rvalue = (performDateCheck ? "1" : "0").concat(echoSettlementData ? "1" : "0").concat(includeLoyaltyData ? "1" : "0");
        if(!StringUtils.isNullOrEmpty(transactionGroupId)) {
            rvalue = rvalue.concat(transactionGroupId);
        }
        rvalue = rvalue.concat(incrementalSupportIndicator != null ? incrementalSupportIndicator ? "Y" : "N" : "N" );
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
