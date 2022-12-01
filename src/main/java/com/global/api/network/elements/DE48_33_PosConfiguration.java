package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class DE48_33_PosConfiguration implements IDataElement<DE48_33_PosConfiguration> {
    private String timezone;
    private Boolean supportsPartialApproval;
    private Boolean supportsReturnBalance;
    private Boolean supportsCashOver;
    private Boolean mobileDevice;
    @Getter @Setter
    private Boolean supportWexAdditionalProducts;

    public String getTimezone() {
        return timezone;
    }
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    public Boolean getSupportsPartialApproval() {
        return supportsPartialApproval;
    }
    public void setSupportsPartialApproval(Boolean supportsPartialApproval) {
        this.supportsPartialApproval = supportsPartialApproval;
    }
    public Boolean getSupportsReturnBalance() {
        return supportsReturnBalance;
    }
    public void setSupportsReturnBalance(Boolean supportsReturnBalance) {
        this.supportsReturnBalance = supportsReturnBalance;
    }
    public Boolean getSupportsCashOver() {
        return supportsCashOver;
    }
    public void setSupportsCashOver(Boolean supportsCashOver) {
        this.supportsCashOver = supportsCashOver;
    }
    public Boolean getMobileDevice() {
        return mobileDevice;
    }
    public void setMobileDevice(Boolean mobileDevice) {
        this.mobileDevice = mobileDevice;
    }

    public DE48_33_PosConfiguration fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        timezone = sp.readString(1);
        supportsPartialApproval = sp.readBoolean("Y");
        supportsReturnBalance = sp.readBoolean("Y");
        supportsCashOver = sp.readBoolean("2");
        mobileDevice = sp.readBoolean("Y");
        supportWexAdditionalProducts = sp.readBoolean("Y");
        return this;
    }

    public byte[] toByteArray() {
        String rvalue = StringUtils.isNullOrEmpty(timezone) ? " " : timezone;
        rvalue = rvalue.concat(supportsPartialApproval == null ? " " : supportsPartialApproval ? "Y" : "N");
        rvalue = rvalue.concat(supportsReturnBalance == null ? " " : supportsReturnBalance ? "Y" : "N");
        rvalue = rvalue.concat(supportsCashOver == null ? " " : supportsCashOver ? "0" : "2");
        rvalue = rvalue.concat(mobileDevice == null ? " " : mobileDevice ? "Y" : "N");
        rvalue = rvalue.concat(supportWexAdditionalProducts == null ? " " : supportWexAdditionalProducts ? "Y" : "N");

        return StringUtils.trimEnd(rvalue).getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
