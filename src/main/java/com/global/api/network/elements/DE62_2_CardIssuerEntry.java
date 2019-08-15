package com.global.api.network.elements;

import com.global.api.network.enums.CardIssuerEntryTag;

public class DE62_2_CardIssuerEntry {
    private CardIssuerEntryTag issuerTag;
    private String issuerTagValue;
    private String issuerEntry;

    public CardIssuerEntryTag getIssuerTag() {
        return issuerTag;
    }
    public void setIssuerTag(CardIssuerEntryTag issuerTag) {
        this.issuerTag = issuerTag;
    }
    public String getIssuerTagValue() {
        return issuerTagValue;
    }
    public void setIssuerTagValue(String issuerTagValue) {
        this.issuerTagValue = issuerTagValue;
    }
    public String getIssuerEntry() {
        return issuerEntry;
    }
    public void setIssuerEntry(String issuerEntry) {
        this.issuerEntry = issuerEntry;
    }

    public DE62_2_CardIssuerEntry() {
        this(null, null);
    }
    public DE62_2_CardIssuerEntry(CardIssuerEntryTag tag, String entry) {
        this(tag, null, entry);
    }
    public DE62_2_CardIssuerEntry(CardIssuerEntryTag tag, String tagValue, String entry) {
        this.issuerTag = tag;
        this.issuerTagValue = tagValue;
        this.issuerEntry = entry;
    }
}
