package com.global.api.utils;

import lombok.var;

import java.util.LinkedHashMap;

public class EmvData {
    private LinkedHashMap<String, TlvData> tlvData;
    private LinkedHashMap<String, TlvData> removedTags;
    private boolean standInStatus;
    private String standInStatusReason;

    public String getAcceptedTagData() {
        if(tlvData.size() == 0) {
            return null;
        }

        String rvalue = "";
        for(TlvData tag: tlvData.values()) {
            rvalue = rvalue.concat(tag.getFullValue());
        }
        return rvalue;
    }
    public LinkedHashMap<String, TlvData> getAcceptedTags() { return tlvData; }
    public String getCardSequenceNumber() {
        if(tlvData.containsKey("5F34")) {
            return tlvData.get("5F34").getValue();
        }
        return null;
    }
    public String getCustomerVerificationResults() {
        TlvData cvm = getTag("9F34");
        if(cvm != null) {
            return cvm.getValue();
        }
        return null;
    }
    public boolean isOfflinePin() {
        String cvr = getCustomerVerificationResults();
        if(!StringUtils.isNullOrEmpty(cvr)) {
            String cvm = cvr.substring(1, 2);
            return (cvm.equals("1") || cvm.equals("3") || cvm.equals("4") || cvm.equals("5"));
        }
        return false;
    }
    public LinkedHashMap<String, TlvData> getRemovedTags() {
        return removedTags;
    }
    public boolean getStandInStatus() {
        return standInStatus;
    }
    public void setStandInStatus(boolean value, String reason) {
        this.standInStatus = value;
        this.standInStatusReason = reason;
    }
    public String getStandInStatusReason() {
        return standInStatusReason;
    }
    public TlvData getTag(String tagName) {
        if(tlvData.containsKey(tagName)) {
            return tlvData.get(tagName);
        }
        return null;
    }

    public byte[] getSendBuffer() {
        return StringUtils.bytesFromHex(getAcceptedTagData());
    }

    public boolean isContactlessMsd() {
        var entryMode = getEntryMode();
        return (entryMode != null) ? entryMode.equals("91") : false;
    }

    public String getEntryMode() {
        var posEntryMode = getTag("9F39");
        return (posEntryMode != null) ? posEntryMode.getValue() : null;
    }

    EmvData() {
        tlvData = new LinkedHashMap<String, TlvData>();
        removedTags = new LinkedHashMap<String, TlvData>();
    }

    void addTag(String tag, String length, String value) {
        addTag(tag, length, value, null);
    }
    void addTag(String tag, String length, String value, String description) {
        addTag(new TlvData(tag, length, value, description));
    }
    void addTag(TlvData tagData) {
        tlvData.put(tagData.getTag(), tagData);
    }

    void addRemovedTag(String tag, String length, String value) {
        addRemovedTag(tag, length, value, null);
    }
    void addRemovedTag(String tag, String length, String value, String description) {
        addRemovedTag(new TlvData(tag, length, value, description));
    }
    void addRemovedTag(TlvData tagData) {
        removedTags.put(tagData.getTag(), tagData);
    }
}
