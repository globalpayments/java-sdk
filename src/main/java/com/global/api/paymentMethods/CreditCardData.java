package com.global.api.paymentMethods;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.DccRateData;
import com.global.api.entities.MerchantDataCollection;
import com.global.api.entities.ThreeDSecure;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.*;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.utils.CardUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class CreditCardData extends Credit implements ICardData {
    @Getter @Setter private String cardHolderName;
    @Getter @Setter private boolean cardPresent = false;
    private String cvn;
    @Getter @Setter private CvnPresenceIndicator cvnPresenceIndicator = CvnPresenceIndicator.NotRequested;
    @Getter @Setter private String eci;
    @Getter @Setter private ManualEntryMethod entryMethod;
    @Getter @Setter private Integer expMonth;
    private Integer expYear;
    private String number;
    private boolean readerPresent = false;

    public String getCvn() {
        return cvn;
    }
    public void setCvn(String cvn) {
        if(cvn != null && !cvn.equals("")) {
            this.cvn = cvn;
            this.cvnPresenceIndicator = CvnPresenceIndicator.Present;
        }
    }
    public Integer getExpYear() {
        return expYear;
    }
    // NOTE: In .NET setExpYear() has a custom logic
    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }
    public String getNumber() {
        return number;
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
            return StringUtils.padLeft(expMonth.toString(), 2, '0') + expYear.toString().substring(2, 4);
        }
        return null;
    }
    public boolean isReaderPresent() {
        return readerPresent;
    }
    public void setReaderPresent(boolean readerPresent) {
        this.readerPresent = readerPresent;
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