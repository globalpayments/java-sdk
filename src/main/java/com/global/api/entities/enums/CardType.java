package com.global.api.entities.enums;

import com.global.api.entities.Card;

public enum CardType {
	VISA,
	MC,
	DISC,
	AMEX,
	GIFTCARD,
	PAYPALECOMMERCE;


	String key;

	CardType(String key) { this.key = key; }

	CardType(){}

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
			default:
				return CardType.PAYPALECOMMERCE;
		}
	}
}

