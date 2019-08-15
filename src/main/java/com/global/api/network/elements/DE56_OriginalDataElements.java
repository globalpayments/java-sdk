package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class DE56_OriginalDataElements implements IDataElement<DE56_OriginalDataElements> {
    private String messageTypeIdentifier;
    private String systemTraceAuditNumber;
    private String transactionDateTime;
    private String acquiringInstitutionId;

    public String getMessageTypeIdentifier() {
        return messageTypeIdentifier;
    }
    public void setMessageTypeIdentifier(String messageTypeIdentifier) {
        this.messageTypeIdentifier = messageTypeIdentifier;
    }
    public String getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }
    public void setSystemTraceAuditNumber(String systemTraceAuditNumber) {
        this.systemTraceAuditNumber = systemTraceAuditNumber;
    }
    public String getTransactionDateTime() {
        return transactionDateTime;
    }
    public void setTransactionDateTime(String transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }
    public String getAcquiringInstitutionId() {
        return acquiringInstitutionId;
    }
    public void setAcquiringInstitutionId(String acquiringInstitutionId) {
        this.acquiringInstitutionId = acquiringInstitutionId;
    }

    public DE56_OriginalDataElements fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        messageTypeIdentifier = sp.readString(4);
        systemTraceAuditNumber = sp.readString(6);
        transactionDateTime = sp.readString(12);
        acquiringInstitutionId = sp.readLLVAR();

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = messageTypeIdentifier
            .concat(systemTraceAuditNumber)
            .concat(transactionDateTime);

        // put the acquirer id if present
        if(acquiringInstitutionId == null) {
            acquiringInstitutionId = "";
        }
        rvalue = rvalue.concat(StringUtils.padLeft(acquiringInstitutionId.length(), 2, '0'))
                .concat(acquiringInstitutionId);

        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
