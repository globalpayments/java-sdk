package com.global.api.paymentMethods;

import com.global.api.entities.enums.EntryMethod;
import com.global.api.entities.enums.TrackNumber;

public interface ITrackData {
    String getExpiry();
    void setExpiry(String value);

    String getPan();
    void setPan(String value);

    TrackNumber getTrackNumber();
    void setTrackNumber(TrackNumber value);

    String getTrackData();
    void setTrackData(String value);

    String getDiscretionaryData();
    void setDiscretionaryData(String value);

    String getValue();
    void setValue(String value);

    EntryMethod getEntryMethod();
    void setEntryMethod(EntryMethod entryMethod);

    String getTruncatedTrackData();
}
