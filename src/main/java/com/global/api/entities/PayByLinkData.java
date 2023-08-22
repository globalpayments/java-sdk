package com.global.api.entities;

import com.global.api.entities.enums.PayByLinkStatus;
import com.global.api.entities.enums.PayByLinkType;
import com.global.api.entities.enums.PaymentMethodUsageMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

@Accessors(chain = true)
@Getter
@Setter
public class PayByLinkData {
    // Describes the type of link that will be created.
    private PayByLinkType type;
    // Indicates whether the link can be used once or multiple times
    private PaymentMethodUsageMode usageMode;
    private String[] allowedPaymentMethods;
    // The number of the times that the link can be used or paid.
    private Integer usageLimit;
    private PayByLinkStatus status;
    // A descriptive name for the link. This will be visible to the customer on the payment page.
    private String name;
    // Indicates if you want to capture the customers shipping information on the hosted payment page.
    // If you enable this field you can also set an optional shipping fee in the shipping_amount.
    private Boolean isShippable;
    // Indicates the cost of shipping when the shippable field is set to YES.
    private BigDecimal shippingAmount;
    // Indicates the date and time after which the link can no longer be used or paid.
    private DateTime expirationDate;
    // Images that will be displayed to the customer on the payment page.
    private List<String> images;
    // The merchant URL that the customer will be redirected to.
    private String returnUrl;
    // The merchant URL (webhook) to notify the merchant of the latest status of the transaction
    private String statusUpdateUrl;
    // The merchant URL that the customer will be redirected to if they chose to cancel
    private String cancelUrl;

    public Boolean isShippable() {
        return isShippable;
    }

    public void isShippable(Boolean shippable) {
        isShippable = shippable;
    }

}