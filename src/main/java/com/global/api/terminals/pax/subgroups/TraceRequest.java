package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class TraceRequest implements IRequestSubGroup {
    private String referenceNumber;
    private String invoiceNumber;
    private String authCode;
    private String transactionNumber;
    private String timeStamp;
    @Setter @Getter
    private String ecrTransactionID;
    @Setter @Getter
    private String clientTransactionId;
    @Setter @Getter
    private String ps2000;
    @Setter @Getter
    private String originalAuthResponse;
    @Setter @Getter
    private String originalTraceNumber;
    @Setter @Getter
    private String cardBrandTransactionId;

    public String getReferenceNumber() {
        return referenceNumber;
    }
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    public String getInvoiceNumber() {
        return invoiceNumber;
    }
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }
    public String getAuthCode() {
        return authCode;
    }
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
    public String getTransactionNumber() {
        return transactionNumber;
    }
    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }
    public String getTimeStamp() {
        return timeStamp;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();
        sb.append(referenceNumber);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(invoiceNumber);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(authCode);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(transactionNumber);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(timeStamp);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(ecrTransactionID);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(clientTransactionId);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(ps2000);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(originalAuthResponse);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(originalTraceNumber);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(cardBrandTransactionId);

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}
