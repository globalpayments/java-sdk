package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.terminals.abstractions.IRequestSubGroup;
import com.global.api.utils.StringUtils;

public class AvsRequest implements IRequestSubGroup {
    private String zipCode;
    private String address;

    public String getZipCode() {
        return zipCode;
    }
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getElementString() {
        StringBuilder sb = new StringBuilder();
        sb.append(zipCode);
        sb.append((char)ControlCodes.US.getByte());
        sb.append(address);

        return StringUtils.trimEnd(sb.toString(), ControlCodes.US);
    }
}