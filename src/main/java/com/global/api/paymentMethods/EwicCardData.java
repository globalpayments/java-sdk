package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.enums.CvnPresenceIndicator;
import com.global.api.entities.enums.InquiryType;
import com.global.api.entities.enums.ManualEntryMethod;
import com.global.api.utils.CardUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class EwicCardData extends Ewic implements ICardData {
    @Getter @Setter private String cardType;
    @Getter @Setter private boolean cardPresent;
    @Getter @Setter private String cvn;
    @Getter @Setter private Integer expMonth;
    @Getter @Setter private Integer expYear;
    private String number;
    @Getter @Setter private boolean readerPresent;
    @Getter @Setter private ManualEntryMethod entryMethod;
    @Getter @Setter private CvnPresenceIndicator cvnPresenceIndicator = CvnPresenceIndicator.NotRequested;
    @Getter @Setter private String tokenizationData;
    @Getter @Setter private String cardHolderName;

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
        try {
            this.cardType = CardUtils.mapCardType(number);
        } catch (Exception e) {
            cardType = "Unknown";
        }
    }
    public String getShortExpiry() {
        if(expMonth != null && expYear != null) {
            return StringUtils.padLeft(expMonth.toString(), 2, '0') + expYear.toString().substring(2, 4);
        }
        return null;
    }
}
