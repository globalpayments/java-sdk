package com.global.api.paymentMethods;

import com.global.api.entities.enums.CvnPresenceIndicator;
import com.global.api.entities.enums.PaymentMethodType;

public class EBTCardData extends EBT implements ICardData {
    private String approvalCode;
    private boolean cardPresent;
    private String cvn;
    private CvnPresenceIndicator cvnPresenceIndicator;
    private Integer expMonth;
    private Integer expYear;
    private String number;
    private boolean readerPresent;
    private String serialNumber;

    public String getApprovalCode() {
        return approvalCode;
    }
    public void setApprovalCode(String approvalCode) {
        this.approvalCode = approvalCode;
    }
    public boolean isCardPresent() {
        return cardPresent;
    }
    public void setCardPresent(boolean cardPresent) {
        this.cardPresent = cardPresent;
    }
    public String getCvn() {
        return cvn;
    }
    public void setCvn(String cvn) {
        this.cvn = cvn;
    }
    public CvnPresenceIndicator getCvnPresenceIndicator() {
        return cvnPresenceIndicator;
    }
    public void setCvnPresenceIndicator(CvnPresenceIndicator cvnPresenceIndicator) {
        this.cvnPresenceIndicator = cvnPresenceIndicator;
    }
    public Integer getExpMonth() {
        return expMonth;
    }
    public void setExpMonth(Integer expMonth) {
        this.expMonth = expMonth;
    }
    public Integer getExpYear() {
        return expYear;
    }
    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public boolean isReaderPresent() {
        return readerPresent;
    }
    public void setReaderPresent(boolean readerPresent) {
        this.readerPresent = readerPresent;
    }
    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
