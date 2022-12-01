package com.global.api.network.enums;

import com.global.api.entities.Customer;
import com.global.api.entities.enums.IStringConstant;

public enum EncryptedFieldMatrix implements IStringConstant {
    Track1("1"),
    Track2("2"),
    Pan("3"),
    CustomerData("03"),
    CustomerDataCSV("04");

    String value;
    EncryptedFieldMatrix(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
