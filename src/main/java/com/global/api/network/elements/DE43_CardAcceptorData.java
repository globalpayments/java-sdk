package com.global.api.network.elements;

import com.global.api.entities.Address;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class DE43_CardAcceptorData implements IDataElement<DE43_CardAcceptorData> {
    private Address address;

    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }

    public DE43_CardAcceptorData fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        Address address = new Address();
        address.setName(sp.readToChar('\\'));
        address.setStreetAddress1(sp.readToChar('\\'));
        address.setCity(sp.readToChar('\\'));
        address.setPostalCode(StringUtils.trimEnd(sp.readString(10)));
        address.setState(StringUtils.trimEnd(sp.readString(3)));
        address.setCountry(sp.readString(3));
        this.address = address;

        return this;
    }

    public byte[] toByteArray() {
        String name = address.getName() != null ? address.getName() : "";
        String street = address.getStreetAddress1() != null ? address.getStreetAddress1() : "";
        String city = address.getCity() != null ? address.getCity() : "";
        String postalCode = address.getPostalCode() != null ? address.getPostalCode() : "";
        String state = address.getState() != null ? address.getState() : "";
        String country = address.getCountry() != null ? address.getCountry() : "";

        String rvalue = name.concat("\\")
                .concat(street).concat("\\")
                .concat(city).concat("\\")
                .concat(StringUtils.padRight(postalCode, 10, ' '))
                .concat(StringUtils.padRight(state, 3, ' '))
                .concat(country);
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
