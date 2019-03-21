package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class DE48_2_HardwareSoftwareConfig implements IDataElement<DE48_2_HardwareSoftwareConfig> {
    private String hardwareLevel;
    private String softwareLevel;
    private String operatingSystemLevel;

    public String getHardwareLevel() {
        return hardwareLevel;
    }
    public void setHardwareLevel(String hardwareLevel) {
        this.hardwareLevel = hardwareLevel;
    }
    public String getSoftwareLevel() {
        return softwareLevel;
    }
    public void setSoftwareLevel(String softwareLevel) {
        this.softwareLevel = softwareLevel;
    }
    public String getOperatingSystemLevel() {
        return operatingSystemLevel;
    }
    public void setOperatingSystemLevel(String operatingSystemLevel) {
        this.operatingSystemLevel = operatingSystemLevel;
    }

    public DE48_2_HardwareSoftwareConfig fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        hardwareLevel = sp.readString(4);
        softwareLevel = sp.readString(8);
        operatingSystemLevel = sp.readString(8);

        return this;
    }

    public byte[] toByteArray() {
        if(StringUtils.isNullOrEmpty(hardwareLevel) && StringUtils.isNullOrEmpty(softwareLevel) && StringUtils.isNullOrEmpty(operatingSystemLevel)) {
            return null;
        }

        String rvalue = StringUtils.isNullOrEmpty(hardwareLevel) ? "    " : StringUtils.padRight(hardwareLevel, 4, ' ');
        rvalue = rvalue.concat(StringUtils.isNullOrEmpty(softwareLevel) ? "        " : StringUtils.padRight(softwareLevel, 8, ' '));
        rvalue = rvalue.concat(StringUtils.isNullOrEmpty(operatingSystemLevel) ? "        " : StringUtils.padRight(operatingSystemLevel, 8, ' '));
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
