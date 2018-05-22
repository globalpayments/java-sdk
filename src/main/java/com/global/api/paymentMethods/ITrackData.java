package com.global.api.paymentMethods;

import com.global.api.entities.enums.EntryMethod;

public interface ITrackData {
    String getValue();
    void setValue(String value);

    EntryMethod getEntryMethod();
    void setEntryMethod(EntryMethod entryMethod);
}
