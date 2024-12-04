package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class Card {
    private String cardHolderName;
    private String cardNumber;
    private String maskedCardNumber;
    private String cardExpMonth;
    private String cardExpYear;
    private String token;

    // Masked card number with last 4 digits showing
    private String maskedNumberLast4;

    // Indicates the card brand that issued the card
    private String brand;

    // The unique reference created by the brands/schemes to uniquely identify the transaction
    private String brandReference;

    // Contains the fist 6 digits of the card
    private String bin;

    // The issuing country that the bin is associated with
    private String binCountry;

    // The card providers description of their card product
    private String accountType;

    // The label of the issuing bank or financial institution of the bin
    private String issuer;

    private String funding;

}