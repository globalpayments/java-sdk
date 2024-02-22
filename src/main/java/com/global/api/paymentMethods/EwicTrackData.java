package com.global.api.paymentMethods;

import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TrackNumber;
import com.global.api.utils.CardUtils;
import lombok.Getter;
import lombok.Setter;

public class EwicTrackData extends Ewic implements  ITrackData {

    @Getter @Setter
    private String discretionaryData;
    @Getter@Setter
    private EntryMethod entryMethod = EntryMethod.Swipe;
    @Getter@Setter
    private String encryptedPan;
    @Getter@Setter
    private String expiry;
    @Getter@Setter
    private String pan;
    @Getter@Setter
    private String pinBlock;
    @Getter
    private String purchaseDeviceSequenceNumber;
    @Getter@Setter
    private TrackNumber trackNumber = TrackNumber.Unknown;
    private String trackData;
    private String value;
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
    }

    @Override
    public String getTokenizationData() {
        return null;
    }

    @Override
    public void setTokenizationData(String s) {

    }

    @Override
    public String getTruncatedTrackData() {
        return null;
    }


}
