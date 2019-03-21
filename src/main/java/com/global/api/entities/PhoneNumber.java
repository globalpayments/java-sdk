package com.global.api.entities;

import com.global.api.utils.StringUtils;

public class PhoneNumber {
    private String countryCode;
    private String areaCode;
    private String number;
    private String extension;

    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public String getAreaCode() {
        return areaCode;
    }
    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public String getExtension() {
        return extension;
    }
    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        // country code (default to 1)
        if(StringUtils.isNullOrEmpty(countryCode)) {
            countryCode = "1";
        }
        sb.append("+".concat(countryCode));

        // append area code if present
        if(!StringUtils.isNullOrEmpty(areaCode)) {
            sb.append(String.format("(%s)", areaCode));
        }

        // put the number
        sb.append(number);

        // put extension if present
        if(!StringUtils.isNullOrEmpty(extension)) {
            sb.append(String.format("EXT: %s", extension));
        }

        return sb.toString();
    }
}
