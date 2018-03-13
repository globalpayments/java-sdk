package com.global.api.paymentMethods;

import com.global.api.entities.enums.EntryMethod;

public class DebitTrackData extends Debit implements ITrackData {
    private EntryMethod entryMethod = EntryMethod.Swipe;
    private String value;

    public EntryMethod getEntryMethod() {
        return entryMethod;
    }
    public void setEntryMethod(EntryMethod entryMethod) {
        this.entryMethod = entryMethod;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
