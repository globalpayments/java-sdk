package com.global.api.paymentMethods;

import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.utils.CardUtils;

public class CreditTrackData extends Credit implements ITrackData {
    private String discretionaryData;
    private EntryMethod entryMethod = EntryMethod.Swipe;
    private String expiry;
    private String pan;
    private String purchaseDeviceSequenceNumber;
    private TrackNumber trackNumber = TrackNumber.Unknown;
    private String trackData;
    private String value;

    public String getDiscretionaryData() {
        return discretionaryData;
    }
    public void setDiscretionaryData(String discretionaryData) {
        this.discretionaryData = discretionaryData;
    }

    public EntryMethod getEntryMethod() {
        return entryMethod;
    }
    public void setEntryMethod(EntryMethod entryMethod) {
        this.entryMethod = entryMethod;
    }

    public String getExpiry() {
        return expiry;
    }
    public  void setExpiry(String value) {
        expiry = value;
    }

    public String getPan() {
        return pan;
    }
    public void setPan(String value) {
        pan = value;
    }

    public String getPurchaseDeviceSequenceNumber() {
        return purchaseDeviceSequenceNumber;
    }

    public TrackNumber getTrackNumber() {
        return trackNumber;
    }
    public void setTrackNumber(TrackNumber value) {
        trackNumber = value;
    }

    public String getTrackData() {
        return trackData;
    }
    public void setTrackData(String value) {
        if(this.value == null) {
            setValue(value);
        }
        else {
            trackData = value;
        }
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
        CardUtils.parseTrackData(this);
        this.cardType = CardUtils.mapCardType(pan);
        this.fleetCard = CardUtils.isFleet(cardType, pan);

        if(cardType.equals("WexFleet") && discretionaryData != null && discretionaryData.length() >= 8) {
            purchaseDeviceSequenceNumber = discretionaryData.substring(3, 8);
        }
    }

    public CreditTrackData() {
        this(null);
    }
    public CreditTrackData(String trackData) {
        if(trackData != null) {
            setValue(trackData);
        }
    }
}
