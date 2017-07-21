package com.global.api.entities;

import com.global.api.entities.enums.EcommerceChannel;
import com.global.api.utils.DateUtils;

import java.util.Date;

public class EcommerceInfo {
    private String cavv;
    private EcommerceChannel channel;
    private String eci;
    private String paymentDataSource;
    private String paymentDataType;
    private Integer shipDay;
    private Integer shipMonth;
    private String xid;

    public String getCavv() {
        return cavv;
    }
    public void setCavv(String cavv) {
        this.cavv = cavv;
    }
    public EcommerceChannel getChannel() {
        return channel;
    }
    public void setChannel(EcommerceChannel channel) {
        this.channel = channel;
    }
    public String getEci() {
        return eci;
    }
    public void setEci(String eci) {
        this.eci = eci;
    }
    public String getPaymentDataSource() {
        return paymentDataSource;
    }
    public void setPaymentDataSource(String paymentDataSource) {
        this.paymentDataSource = paymentDataSource;
    }
    public String getPaymentDataType() {
        return paymentDataType;
    }
    public void setPaymentDataType(String paymentDataType) {
        this.paymentDataType = paymentDataType;
    }
    public Integer getShipDay() {
        return shipDay;
    }
    public void setShipDay(int shipDay) {
        this.shipDay = shipDay;
    }
    public Integer getShipMonth() {
        return shipMonth;
    }
    public void setShipMonth(int shipMonth) {
        this.shipMonth = shipMonth;
    }
    public String getXid() {
        return xid;
    }
    public void setXid(String xid) {
        this.xid = xid;
    }

    public EcommerceInfo() {
        Date tomorrow = DateUtils.addDays(new Date(), 1);
        this.channel = EcommerceChannel.Ecom;
        this.shipDay = tomorrow.getDay();
        this.shipMonth = tomorrow.getMonth();
        this.paymentDataType = "3DSecure";
    }
}
