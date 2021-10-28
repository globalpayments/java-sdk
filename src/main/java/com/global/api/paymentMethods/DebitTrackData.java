package com.global.api.paymentMethods;

import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.utils.CardUtils;

public class DebitTrackData extends Debit implements ITrackData {
    private String discretionaryData;
    private String encryptedPan;
    private EntryMethod entryMethod = EntryMethod.Swipe;
    private String expiry;
    private String pan;
    private TrackNumber trackNumber;
    private String trackData;
    private String value;

    public String getDiscretionaryData() {
        return discretionaryData;
    }
    public void setDiscretionaryData(String discretionaryData) {
        this.discretionaryData = discretionaryData;
    }

    public String getEncryptedPan() {
        return encryptedPan;
    }
    public void setEncryptedPan(String value) {
        encryptedPan = value;
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

    public String getTruncatedTrackData() {
        if(discretionaryData != null) {
            return trackData.replace(discretionaryData, "");
        }
        return trackData;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
        CardUtils.parseTrackData(this);
        this.cardType = CardUtils.mapCardType(pan);
    }
}
