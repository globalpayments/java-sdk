package com.global.api.entities.gpApi.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class FundsAccountDetails {
    private String id;
    private String status;
    private String timeCreated;
    private String timeLastUpdated;
    private String amount;
    private String reference;
    private String description;

    private String currency;
    private String paymentMethodType;
    private String paymentMethodName;
    private UserAccount account;
}