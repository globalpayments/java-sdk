package com.global.api.entities.enums;

public enum CardType {
    VISA,
    MC,
    DISC,
    AMEX,
    GIFTCARD,
    DEBIT,
    PAYPALECOMMERCE;


    String key;

    CardType(String key) {
        this.key = key;
    }

    CardType() {
    }

    public static CardType getValue(String x) {
        switch (x) {
            case "visa":
                return CardType.VISA;
            case "mastercard":
                return CardType.MC;
            case "discover":
                return CardType.DISC;
            case "amex":
                return CardType.AMEX;
            case "giftcard":
                return CardType.GIFTCARD;
            case "debit":
                return CardType.DEBIT;
            default:
                return CardType.PAYPALECOMMERCE;
        }
    }
}

