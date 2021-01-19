package com.global.api.entities.reporting;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class DepositSummary {
    public String depositId;
    public String merchantHierarchy;
    public String merchantName;
    public String merchantDbaName;
    public String merchantNumber;
    public String merchantCategory;
    public Date depositDate;
    public String reference;
    public BigDecimal amount;
    public String currency;
    public String type;
    public String routingNumber;
    public String accountNumber;
    public String mode;
    public String summaryModel;
    public int salesTotalCount;
    public BigDecimal salesTotalAmount;
    public String salesTotalCurrency;
    public int refundsTotalCount;
    public BigDecimal refundsTotalAmount;
    public String refundsTotalCurrency;
    public int chargebackTotalCount;
    public BigDecimal chargebackTotalAmount;
    public String chargebackTotalCurrency;
    public int representmentTotalCount;
    public BigDecimal representmentTotalAmount;
    public String representmentTotalCurrency;
    public BigDecimal feesTotalAmount;
    public String feesTotalCurrency;
    public int adjustmentTotalCount;
    public BigDecimal adjustmentTotalAmount;
    public String adjustmentTotalCurrency;
    public String status;
}
