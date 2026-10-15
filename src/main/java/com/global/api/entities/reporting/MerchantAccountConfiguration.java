package com.global.api.entities.reporting;

import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.PaymentMethodName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MerchantAccountConfiguration {
    private List<String> countries;
    private List<String> currencies;
    private List<Channel> channels;
    private List<PaymentMethodName> paymentMethods;
}