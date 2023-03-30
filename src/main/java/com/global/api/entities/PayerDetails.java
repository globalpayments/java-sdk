package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class PayerDetails {
    private String firstName;
    private String lastName;
    private String email;
    private Address billingAddress;
    private Address shippingAddress;
}