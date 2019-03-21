package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE48_NameFormat;
import com.global.api.network.enums.DE48_NameType;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class DE48_Name implements IDataElement<DE48_Name> {
    private DE48_NameType nameType;
    private DE48_NameFormat nameFormat;
    private String name;

    private String prefix;
    private String firstName;
    private String middleName;
    private String lastName;
    private String suffix;
    private String positionalTitle;
    private String functionalTitle;

    public DE48_NameType getNameType() {
        return nameType;
    }
    public void setNameType(DE48_NameType nameType) {
        this.nameType = nameType;
    }
    public DE48_NameFormat getNameFormat() {
        return nameFormat;
    }
    public void setNameFormat(DE48_NameFormat nameFormat) {
        this.nameFormat = nameFormat;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getMiddleName() {
        return middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getSuffix() {
        return suffix;
    }
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    public String getPositionalTitle() {
        return positionalTitle;
    }
    public void setPositionalTitle(String positionalTitle) {
        this.positionalTitle = positionalTitle;
    }
    public String getFunctionalTitle() {
        return functionalTitle;
    }
    public void setFunctionalTitle(String functionalTitle) {
        this.functionalTitle = functionalTitle;
    }

    public DE48_Name fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        nameType = sp.readStringConstant(1, DE48_NameType.class);
        nameFormat = sp.readStringConstant(1, DE48_NameFormat.class);

        switch(nameFormat) {
            case Delimited_FirstMiddleLast: {
                firstName = sp.readToChar('\\');
                middleName = sp.readToChar('\\');
                lastName = sp.readRemaining();
            } break;
            case Delimited_Title: {
                prefix = sp.readToChar('\\');
                firstName = sp.readToChar('\\');
                middleName = sp.readToChar('\\');
                lastName = sp.readToChar('\\');
                suffix = sp.readToChar('\\');
                positionalTitle = sp.readToChar('\\');
                functionalTitle = sp.readRemaining();
            } break;
            default: {
                name = sp.readRemaining();
            }
        }

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = nameType.getValue()
                .concat(nameFormat.getValue());

        switch (nameFormat) {
            case Delimited_FirstMiddleLast: {
                rvalue = rvalue.concat(firstName + '\\')
                        .concat(middleName + '\\')
                        .concat(lastName);
            } break;
            case Delimited_Title: {
                rvalue = rvalue.concat(prefix + '\\')
                        .concat(firstName + '\\')
                        .concat(lastName + '\\')
                        .concat(suffix + '\\')
                        .concat(positionalTitle + '\\')
                        .concat(functionalTitle);
            } break;
            default: {
                rvalue = rvalue.concat(name);
            }
        }

        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
