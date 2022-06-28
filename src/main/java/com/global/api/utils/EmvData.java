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

    public String getTransactionCurrencyCode() {
        TlvData currencyCode=getTag("5F2A");
        if(currencyCode!=null) {
            return currencyCode.getValue();
        }
        return null;
    }

    public String getCryptogramInformationData() {
        TlvData data=getTag("9F27");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getTerminalCountryCode() {
        TlvData data=getTag("9F1A");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getEMVTransactionDate() {
        TlvData data=getTag("9A");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getEMVTrack2Data() {
        TlvData data=getRemoveTag("57");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getApplicationCryptogram() {
        TlvData data=getTag("9F26");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getApplicationInterchangeProfile() {
        TlvData data=getTag("82");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getApplicationTransactionCounter() {
        TlvData data=getTag("9F36");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getUnpredictableNumber() {
        TlvData data=getTag("9F37");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getTerminalVerificationResults() {
        TlvData data=getTag("95");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getEMVTransactionType() {
        TlvData data=getTag("9C");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getCryptogramAmount() {
        TlvData data=getTag("9F02");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getIssuerApplicationData() {
        TlvData data=getTag("9F10");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getEMVTerminalType() {
        TlvData data=getTag("9F35");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getApplicationVersionNumber() {
        TlvData data=getTag("9F09");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getDedicatedFileName() {
        TlvData data=getTag("84");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }

    public String getFormFactorIndicator() {
        TlvData data=getTag("9F6E");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }

    public String getLanguageCode() {
        TlvData data = getTag("5F2D");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }
    public String getIsoResponseCode() {
        TlvData data=getTag("8A");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }

    public String getDeviceTypeIndicator(){
        TlvData data=getTag("9F63");
        if(data!=null) {
            return data.getValue();
        }
        return null;
    }

    public String getApprovalCode(){
        TlvData data=getTag("89");
        if(data!=null) {
            return data.getValue();
        }
        return null;
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

    public TlvData getRemoveTag(String tagName) {
        if(removedTags.containsKey(tagName)) {
            return removedTags.get(tagName);
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
