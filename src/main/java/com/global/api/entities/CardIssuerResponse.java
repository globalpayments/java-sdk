package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class CardIssuerResponse {
    // The result code of the AVS check from the card issuer
    public String avsResult;
    // Result code from the card issuer
    public String result;
    // The result code of the CVV check from the card issuer
    public String cvvResult;
    // The result code of the AVS address check from the card issuer
    public String avsAddressResult;
    // The result of the AVS postal code check from the card issuer
    public String avsPostalCodeResult;
}