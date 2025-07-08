package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.entities.DccRateData;
import com.global.api.entities.enums.*;
import com.global.api.utils.CardUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CreditCardData extends Credit implements ICardData {
    @Setter private String cardHolderName;
    @Setter private boolean cardPresent = false;
    private String cvn;
    @Setter private CvnPresenceIndicator cvnPresenceIndicator = CvnPresenceIndicator.NotRequested;
    @Setter private String eci;
    @Setter private ManualEntryMethod entryMethod;
    @Setter private Integer expMonth;
    private Integer expYear;
    private String number;
    @Setter boolean readerPresent = false;
    @Setter private String tokenizationData;

    public void setCvn(String cvn) {
        if(cvn != null && !cvn.isEmpty()) {
            this.cvn = cvn;
            this.cvnPresenceIndicator = CvnPresenceIndicator.Present;
        }
    }
    public void setExpYear(Integer expYear) {
        if(expYear != null) {
            // if it's 2 digit... make it 4
            if(Math.floor(Math.log10(expYear)) + 1 == 2) {
                this.expYear = expYear + 2000;
            }
            else this.expYear = expYear;
        }
    }
    public void setNumber(String number) {
        this.number = number;
        try {
            this.cardType = CardUtils.mapCardType(number);
            this.fleetCard = CardUtils.isFleet(cardType, number);
        } catch (Exception e) {
            cardType = "Unknown";
        }
    }
    public String getShortExpiry() {
        if(expMonth != null && expYear != null) {
            if(expYear.toString().length() > 2) {
                return StringUtils.padLeft(expMonth.toString(), 2, '0') + expYear.toString().substring(2, 4);
            }
            else return StringUtils.padLeft(expMonth.toString(), 2, '0') + expYear;
        }
        return null;
    }

    public CreditCardData() {
    }
    public CreditCardData(String token) {
        this();
        this.setToken(token);
    }

    public AuthorizationBuilder getDccRate() {
        return getDccRate(null, null);
    }

    public AuthorizationBuilder getDccRate(DccRateType dccRateType, DccProcessor dccProcessor) {
        DccRateData dccRateData = new DccRateData();
        dccRateData.setDccRateType(dccRateType == null ? DccRateType.None : dccRateType);
        dccRateData.setDccProcessor(dccProcessor == null ? DccProcessor.None : dccProcessor);

        return new AuthorizationBuilder(TransactionType.DccRateLookup, this)
                .withDccRateData(dccRateData);
    }

    public boolean hasInAppPaymentData() {
        return !StringUtils.isNullOrEmpty(this.getToken()) && this.getMobileType() != null;
    }

}