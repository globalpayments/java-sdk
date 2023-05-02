package com.global.api.entities.reporting;

import com.global.api.entities.Address;
import com.global.api.entities.enums.Channel;
import com.global.api.entities.enums.MerchantAccountStatus;
import com.global.api.entities.enums.MerchantAccountType;
import com.global.api.entities.enums.PaymentMethodName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MerchantAccountSummary {
    private String id;
    private MerchantAccountType type;
    private String name;
    private MerchantAccountStatus status;
    private List<Channel> channels;
    private List<String> permissions;
    private List<String> countries;
    private List<String> currencies;
    private List<PaymentMethodName> paymentMethods;
    private List<String> configurations;
    private List<Address> addresses;
}