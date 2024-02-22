package com.global.api.network.enums;

import com.global.api.entities.enums.IStringConstant;

public enum PurchaseType implements IStringConstant {

    NOVISAFLEET2DOT0(" "),
	Fuel("1"),
	NonFuel("2"),
	FuelAndNonFuel("3");
	
	private final String value;
	PurchaseType(String value) { this.value = value; }
    public String getValue() {
        return value;
    }
    public byte[] getBytes() {
        return value.getBytes();
    }
}
