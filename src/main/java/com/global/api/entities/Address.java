package com.global.api.entities;

import com.global.api.entities.enums.AddressType;
import com.global.api.utils.CountryUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class Address {
    private AddressType type;
    private String streetAddress1;
    private String streetAddress2;
    private String streetAddress3;
    private String city;
    private String name;
    private String province;
    private String postalCode;
    private String country;
    private String countryCode;

    public String getState() {
        return province;
    }
    public void setState(String province) {
        this.province = province;
    }

    public Address setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        if(this.country == null)
            this.country = CountryUtils.getCountryByCode(countryCode);

        return this;
    }

    public Address setCountry(String country) {
        this.country = country;
        if (this.countryCode == null)
            this.countryCode = CountryUtils.getCountryCodeByCountry(country);

        return this;
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
