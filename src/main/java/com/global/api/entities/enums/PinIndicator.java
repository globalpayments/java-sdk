package com.global.api.entities.enums;

public enum PinIndicator implements IStringConstant{
	WithoutPin("0"),
	WithPin("1"),
	NotPromptedPin("2"),
	PromptedPin("3"),
	PinValidate("4");

	String value;
	PinIndicator(String value) {
        this.value = value;
    }
	
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
    
}
