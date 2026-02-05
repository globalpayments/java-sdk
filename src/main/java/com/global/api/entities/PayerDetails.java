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
    private String name;
    private String email;
    private String country;
    private String landlinePhone;
    private String mobilePhone;
    private Address billingAddress;
    private Address shippingAddress;
    private String taxIdReference;
}
