package com.global.api.network.enums.nts;

import com.global.api.entities.enums.IStringConstant;

public enum UserDataSubTag implements IStringConstant {
    FleetWorkOrderNumber("1"),
    FleetTrailerNumber("2"),
    FleetEmployeeNumber("3"),
    FleetAdditionalPromptData1("4"),
    FleetAdditionalPromptData2("5");

    String value;
    UserDataSubTag(String value){
        this.value = value;
    }
    @Override
    public byte[] getBytes() {
        return value.getBytes();
    }

    @Override
    public String getValue() {
        return value;
    }
}
