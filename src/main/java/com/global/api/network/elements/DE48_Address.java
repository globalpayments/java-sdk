package com.global.api.network.elements;

import com.global.api.entities.Address;
import com.global.api.entities.PhoneNumber;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE48_AddressType;
import com.global.api.network.enums.DE48_AddressUsage;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class DE48_Address implements IDataElement<DE48_Address> {
    private char paddingChar = ' ';
    private DE48_AddressType addressType;
    private DE48_AddressUsage addressUsage;
    private Address address;
    private PhoneNumber phoneNumber;
    private String email;

    public DE48_AddressType getAddressType() {
        return addressType;
    }
    public void setAddressType(DE48_AddressType addressType) {
        this.addressType = addressType;
    }
    public DE48_AddressUsage getAddressUsage() {
        return addressUsage;
    }
    public void setAddressUsage(DE48_AddressUsage addressUsage) {
        this.addressUsage = addressUsage;
    }
    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }
    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public DE48_Address fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        addressType = sp.readStringConstant(1, DE48_AddressType.class);
        addressUsage = sp.readStringConstant(1, DE48_AddressUsage.class);

        String remainder = sp.readRemaining();
        if(remainder.contains("^")) {
            paddingChar = '^';
        }

        sp = new StringParser(remainder.getBytes());
        switch(addressType) {
            case StreetAddress: {
                address = new Address();
                address.setStreetAddress1(sp.readToChar('\\'));
                address.setStreetAddress2(sp.readToChar('\\'));
                address.setCity(sp.readToChar('\\'));
                address.setState(StringUtils.trimEnd(sp.readString(3), " ", "^"));
                address.setPostalCode(StringUtils.trimEnd(sp.readString(10), " ", "^"));
                address.setCountry(StringUtils.trimEnd(sp.readString(3), " ", "^"));
            } break;
            case AddressVerification: {
                address = new Address();
                address.setPostalCode(StringUtils.trimEnd(sp.readString(9), " ", "^"));
                address.setStreetAddress1(sp.readRemaining());
            } break;
            case PhoneNumber: {
                phoneNumber = new PhoneNumber();
                phoneNumber.setCountryCode(sp.readToChar('\\'));
                phoneNumber.setAreaCode(sp.readToChar('\\'));
                phoneNumber.setNumber(sp.readToChar('\\'));
                phoneNumber.setExtension(sp.readRemaining());
            } break;
            case Email: {
                email = sp.readRemaining();
            } break;
            case AddressVerification_Numeric: {
                address = new Address();
                address.setPostalCode(StringUtils.trimEnd(sp.readString(9)));
                address.setStreetAddress1(StringUtils.trimEnd(sp.readString(6)));
            } break;
        }

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = addressType.getValue()
                .concat(addressUsage.getValue());

        switch (addressType) {
            case StreetAddress: {
                // street 1
                if(address.getStreetAddress1() != null) {
                    rvalue = rvalue.concat(address.getStreetAddress1());
                }
                rvalue = rvalue.concat("\\");

                // street 2
                if(address.getStreetAddress2() != null) {
                    rvalue = rvalue.concat(address.getStreetAddress2());
                }
                rvalue = rvalue.concat("\\");

                // city
                if(address.getCity() != null) {
                    rvalue = rvalue.concat(address.getCity());
                }
                rvalue = rvalue.concat("\\");

                rvalue = rvalue.concat(StringUtils.padRight(address.getState(), 3, ' '))
                        .concat(StringUtils.padRight(address.getPostalCode(), 10, paddingChar))
                        .concat(StringUtils.padRight(address.getCountry(), 3, paddingChar));
            } break;
            case AddressVerification: {
                rvalue = rvalue.concat(StringUtils.padRight(address.getPostalCode(), 9, paddingChar));
                if(!StringUtils.isNullOrEmpty(address.getStreetAddress1())) {
                    rvalue = rvalue.concat(address.getStreetAddress1());
                }
            } break;
            case PhoneNumber: {
                rvalue = rvalue.concat(StringUtils.join("\\", new String[] {
                        phoneNumber.getCountryCode(),
                        phoneNumber.getAreaCode(),
                        phoneNumber.getNumber(),
                        phoneNumber.getExtension()
                }));
            } break;
            case Email: {
                rvalue = rvalue.concat(email);
            } break;
            case AddressVerification_Numeric: {
                rvalue = rvalue.concat(StringUtils.padRight(address.getPostalCode(), 9, ' '))
                        .concat(StringUtils.padRight(address.getStreetAddress1(), 6, ' '));
            } break;
        }

        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
