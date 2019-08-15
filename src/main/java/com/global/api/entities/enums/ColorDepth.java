package com.global.api.entities.enums;

public enum ColorDepth implements IStringConstant {
    OneBit("ONE_BIT"),
    TwoBit("TWO_BITS"),
    FourBit("FOUR_BITS"),
    EightBit("EIGHT_BITS"),
    FifteenBit("FIFTEEN_BITS"),
    SixteenBit("SIXTEEN_BITS"),
    TwentyFourBit("TWENTY_FOUR_BITS"),
    ThirtyTwoBit("THIRTY_TWO_BITS"),
    FortyEightBit("FORTY_EIGHT_BITS");

    String value;
    ColorDepth(String value) { this.value = value; }
    public String getValue() { return value; }
    public byte[] getBytes() { return value.getBytes(); }
}
