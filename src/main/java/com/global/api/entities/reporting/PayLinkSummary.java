package com.global.api.entities.reporting;

import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.PayLinkStatus;
import com.global.api.entities.enums.PayLinkType;
import com.global.api.entities.enums.PaymentMethodName;
import com.global.api.entities.enums.PaymentMethodUsageMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class PayLinkSummary {
    private String merchantId;
    private String merchantName;
    private String accountId;
    private String accountName;
    private String id;
    private String url;
    private PayLinkStatus status;
    private PayLinkType type;
    private List<PaymentMethodName> allowedPaymentMethods;
    private PaymentMethodUsageMode usageMode;
    private String usageCount;
    private String reference;
    private String name;
    private String description;
    private String shippable;
    public String shippingAmount;
    private String viewedCount;
    private DateTime expirationDate;
    private List<String> images;
    public BigDecimal amount;
    public String currency;
    private List<TransactionSummary> transactions = new ArrayList<>();
}