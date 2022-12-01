package com.global.api.entities;

public class EncryptionData {
    private String version;
    private String trackNumber;
    private String ksn;
    private String ktb;

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getTrackNumber() {
        return trackNumber;
    }
    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }
    public String getKsn() {
        return ksn;
    }
    public void setKsn(String ksn) {
        this.ksn = ksn;
    }
    public String getKtb() {
        return ktb;
    }
    public void setKtb(String ktb) {
        this.ktb = ktb;
    }

    public static EncryptionData version1() {
        EncryptionData rvalue = new EncryptionData();
        rvalue.setVersion("01");
        return rvalue;
    }
    public static EncryptionData version2(String ktb) {
        return version2(ktb, null);
    }
    public static EncryptionData version2(String ktb, String trackNumber) {
        EncryptionData rvalue = new EncryptionData();
        rvalue.setVersion("02");
        rvalue.setTrackNumber(trackNumber);
        rvalue.setKtb(ktb);
        return rvalue;
    }

    public static EncryptionData add(String ksn,String trackNumber){
        EncryptionData rvalue = new EncryptionData();
        rvalue.setTrackNumber(trackNumber);
        rvalue.setKsn(ksn);
        return rvalue;
    }

    public static EncryptionData setKtbAndKsn(String ktb,String ksn){
        EncryptionData rvalue = new EncryptionData();
        rvalue.setKtb(ktb);
        rvalue.setKsn(ksn);
        return rvalue;
    }
}
