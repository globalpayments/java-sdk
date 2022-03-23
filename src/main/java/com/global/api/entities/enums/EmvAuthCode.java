package com.global.api.entities.enums;

public enum EmvAuthCode implements IStringConstant{
	OfflineApproved("Y1"),
	OfflineDeclined("Z1"),
	UnableToGoOnlineOfflineApproved("Y3"),
	UnableToGoOnlineOfflineDeclined("Z3");
	
	String value;
	EmvAuthCode(String value) {
        this.value = value;
    }
    public String getValue() { return this.value; }
    public byte[] getBytes() { return this.value.getBytes(); }
}
