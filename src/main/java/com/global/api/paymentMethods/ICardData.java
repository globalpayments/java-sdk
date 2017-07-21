package com.global.api.paymentMethods;

import com.global.api.entities.enums.CvnPresenceIndicator;

public interface ICardData {
    boolean isCardPresent();
    void setCardPresent(boolean cardPresent);

    String getCvn();
    void setCvn(String cvn);

    CvnPresenceIndicator getCvnPresenceIndicator();
    void setCvnPresenceIndicator(CvnPresenceIndicator cvnPresenceIndicator);

    String getNumber();
    void setNumber(String number);

    Integer getExpMonth();
    void setExpMonth(Integer expMonth);

    Integer getExpYear();
    void setExpYear(Integer expYear);

    boolean isReaderPresent();
    void setReaderPresent(boolean readerPresent);
}
