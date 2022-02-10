package com.global.api.entities;

import com.global.api.entities.enums.DccProcessor;
import com.global.api.entities.enums.DccRateType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class DccRateData {
    private BigDecimal cardHolderAmount;
    private String cardHolderCurrency;
    private String cardHolderRate;
    private String commissionPercentage;
    private String dccId;
    private DccProcessor dccProcessor;
    private DccRateType dccRateType;
    private String orderId;
    private String exchangeRateSourceName;
    private DateTime exchangeRateSourceTimestamp;
    private BigDecimal merchantAmount;
    private String merchantCurrency;
    private String marginRatePercentage;
}
