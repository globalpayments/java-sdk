package com.global.api.network.elements;


import com.global.api.entities.enums.IStringConstant;

import java.nio.charset.StandardCharsets;

public enum DE62_COO_2_VerificationType implements IStringConstant {
    RAW_MICR_DATA("1"),
    FORMATTED_MICR_DATA("2"),
    DRIVER_LICENSE("3"),
    FORMATTED_MICR_DRIVER_LICENSE("4"),
    FORMATTED_MICR_ALTERNATE_ID("5"),
    FORMATTED_MICR_DRIVER_LICENSE_ALTERNATE_ID("6"),
    RAW_MICR_DRIVER_LICENSE("7"),
    RAW_MICR_ALTERNATE_ID("8"),
    RAW_MICR_DRIVER_LICENSE_ALTERNATE_ID("9");

    private String value;
    DE62_COO_2_VerificationType(String value) {this.value = value; }

    public byte[] getBytes() {return value.getBytes(StandardCharsets.UTF_8);}

    public String getValue() {return value;}
}
