package com.global.api.network.enums;

import com.global.api.entities.enums.INumericConstant;

public enum SecurityData implements INumericConstant{
	NoAVSAndNoCVN(0),
	AVS(1),
	CVN(2),
	AVSAndCBN(3);
	
	private final int value;
	SecurityData(int value) { this.value = value; }
    public int getValue() {
        return value;
    }
	
}
