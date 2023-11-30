package com.global.api.entities.billing;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class TokenData {
    private DateTime lastUsedDateUTC;
    private boolean isExpired;
    private boolean sharedTokenWithGroup;
    private List<String> merchants;

    public TokenData() {
        merchants = new ArrayList<>();
    }

    public DateTime getLastUsedDateUTC() {
        return lastUsedDateUTC;
    }

    public void setLastUsedDateUTC(DateTime lastUsedDateUTC) {
        this.lastUsedDateUTC = lastUsedDateUTC;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }

    public boolean isSharedTokenWithGroup() {
        return sharedTokenWithGroup;
    }

    public void setSharedTokenWithGroup(boolean sharedTokenWithGroup) {
        this.sharedTokenWithGroup = sharedTokenWithGroup;
    }

    public List<String> getMerchants() {
        return merchants;
    }

    public void setMerchants(List<String> merchants) {
        this.merchants = merchants;
    }
}
