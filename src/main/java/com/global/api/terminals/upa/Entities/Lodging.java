package com.global.api.terminals.upa.Entities;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.math.BigDecimal;
@Getter @Setter
public class Lodging {
    private Integer folioNumber;
    private Integer stayDuration;
    private String checkInDate;
    private String checkOutDate;
    private BigDecimal dailyRate;
    private Integer preferredCustomer;
    private int [] extraChargeTypes;
    private BigDecimal extraChargeTotal;


}
