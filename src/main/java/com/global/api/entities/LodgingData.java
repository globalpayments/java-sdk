package com.global.api.entities;

import com.global.api.entities.enums.AdvancedDepositType;
import com.global.api.entities.enums.ExtraChargeType;
import com.global.api.entities.enums.PrestigiousPropertyLimit;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.HashMap;

public class LodgingData {
    private AdvancedDepositType advancedDepositType;
    private DateTime checkInDate;
    private DateTime checkOutDate;
    private HashMap<ExtraChargeType, BigDecimal> extraCharges;
    private String folioNumber;
    private boolean noShow = false;
    private boolean preferredCustomer = false;
    private PrestigiousPropertyLimit prestigiousPropertyLimit;
    private BigDecimal rate;
    private Integer stayDuration;
    private String lodgingDataEdit;

    public AdvancedDepositType getAdvancedDepositType() {
        return advancedDepositType;
    }
    public void setAdvancedDepositType(AdvancedDepositType advancedDepositType) {
        this.advancedDepositType = advancedDepositType;
    }
    public DateTime getCheckInDate() {
        return checkInDate;
    }
    public void setCheckInDate(DateTime checkInDate) {
        this.checkInDate = checkInDate;
    }
    public DateTime getCheckOutDate() {
        return checkOutDate;
    }
    public void setCheckOutDate(DateTime checkOutDate) {
        this.checkOutDate = checkOutDate;
    }
    public String getFolioNumber() {
        return folioNumber;
    }
    public void setFolioNumber(String folioNumber) {
        this.folioNumber = folioNumber;
    }
    public boolean isNoShow() {
        return noShow;
    }
    public void setNoShow(boolean noShow) {
        this.noShow = noShow;
    }
    public boolean isPreferredCustomer() {
        return preferredCustomer;
    }
    public void setPreferredCustomer(boolean preferredCustomer) {
        this.preferredCustomer = preferredCustomer;
    }
    public PrestigiousPropertyLimit getPrestigiousPropertyLimit() {
        return prestigiousPropertyLimit;
    }
    public void setPrestigiousPropertyLimit(PrestigiousPropertyLimit prestigiousPropertyLimit) {
        this.prestigiousPropertyLimit = prestigiousPropertyLimit;
    }
    public BigDecimal getRate() {
        return rate;
    }
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }
    public Integer getStayDuration() {
        return stayDuration;
    }
    public void setStayDuration(Integer stayDuration) {
        this.stayDuration = stayDuration;
    }
    public HashMap<ExtraChargeType, BigDecimal> getExtraCharges() {
        return extraCharges;
    }
    public BigDecimal getExtraChargeAmount() {
        BigDecimal total = new BigDecimal("0");
        for(BigDecimal amount: extraCharges.values()) {
            total = total.add(amount);
        }
        return total;
    }
    public String getLodgingDataEdit() { return lodgingDataEdit; }
    public void setLodgingDataEdit(String value) { lodgingDataEdit = value; }

    public LodgingData addExtraCharge(ExtraChargeType extraChargeType) {
        return addExtraCharge(extraChargeType, new BigDecimal("0"));
    }
    public LodgingData addExtraCharge(ExtraChargeType extraChargeType, BigDecimal amount) {
        if(extraCharges == null) {
            extraCharges = new HashMap<ExtraChargeType, BigDecimal>();
        }

        if(!extraCharges.containsKey(extraChargeType)) {
            extraCharges.put(extraChargeType, new BigDecimal("0"));
        }

        extraCharges.put(extraChargeType, extraCharges.get(extraChargeType).add(amount));
        return this;
    }
}
