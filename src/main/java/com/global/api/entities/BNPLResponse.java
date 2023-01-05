package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class BNPLResponse {
    private String providerName;
    // URL to redirect the customer, sent so merchant can redirect consumer to complete the payment
    private String redirectUrl;
}