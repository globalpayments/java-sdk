package com.global.api.network.entities.nts;


import com.global.api.entities.enums.IStringConstant;

public enum Nts3DESAndTokenizationDataTag implements IStringConstant {
    Encryption_3DES("3DE"),
    E3_EncryptedData("E3E"),
    Tokenization_TOK("TOK");
    private final String value;
    Nts3DESAndTokenizationDataTag(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }

}
