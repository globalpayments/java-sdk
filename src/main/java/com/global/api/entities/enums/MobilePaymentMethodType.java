package com.global.api.entities.enums;

public enum MobilePaymentMethodType {

	APPLEPAY("apple-pay"),
	GOOGLEPAY("pay-with-google"),
	CLICK_TO_PAY("click-to-pay");
		
	private String value;
	MobilePaymentMethodType(String value){
        this.value = value;
    }
    public String getValue() { return this.value; }

}
