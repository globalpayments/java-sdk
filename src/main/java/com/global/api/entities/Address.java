package com.global.api.entities;

import com.global.api.entities.enums.AddressType;
import com.global.api.utils.CountryUtils;

public class Address {
    private AddressType type;
    private String streetAddress1;
    private String streetAddress2;
    private String streetAddress3;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private String countryCode;

	public AddressType getType() {
        return type;
    }
    public void setType(AddressType type) {
        this.type = type;
    }
    public String getStreetAddress1() {
        return streetAddress1;
    }
    public void setStreetAddress1(String streetAddress1) {
        this.streetAddress1 = streetAddress1;
    }
    public String getStreetAddress2() {
        return streetAddress2;
    }
    public void setStreetAddress2(String streetAddress2) {
        this.streetAddress2 = streetAddress2;
    }
    public String getStreetAddress3() {
        return streetAddress3;
    }
    public void setStreetAddress3(String streetAddress3) {
        this.streetAddress3 = streetAddress3;
    }
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }
    public String getProvince() {
        return province;
    }
    public void setProvince(String province) {
        this.province = province;
    }
    public String getState() {
        return province;
    }
    public void setState(String province) {
        this.province = province;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String code) {
        this.postalCode = code;
    }
    public String getCountryCode() {
       return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        if(this.country == null)
            this.country = CountryUtils.getCountryByCode(countryCode);
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
        if(this.countryCode == null)
            this.countryCode = CountryUtils.getCountryCodeByCountry(country);
    }
    public boolean isCountry(String countryCode) {
        return CountryUtils.isCountry(this, countryCode);
	}

    public Address() { this(null); }
    public Address(String code) {
        this(null, code);
    }
    public Address(String streetAddress1, String code) {
        this.streetAddress1 = streetAddress1;
        this.postalCode = code;
    }
}
