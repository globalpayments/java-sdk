package com.global.api.entities;

import com.global.api.entities.enums.EcommerceChannel;
import com.global.api.utils.DateUtils;
import org.joda.time.DateTime;

import java.util.Date;

public class EcommerceInfo {
    private EcommerceChannel channel;
    private Integer shipDay;
    private Integer shipMonth;

    public EcommerceChannel getChannel() {
        return channel;
    }
    public void setChannel(EcommerceChannel channel) {
        this.channel = channel;
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

    public EcommerceInfo() {
        DateTime tomorrow = DateTime.now().plusDays(1);
        this.channel = EcommerceChannel.Ecom;
        this.shipDay = tomorrow.getDayOfMonth();
        this.shipMonth = tomorrow.getMonthOfYear();
    }
}
