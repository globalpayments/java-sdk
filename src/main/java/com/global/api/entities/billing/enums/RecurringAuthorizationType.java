package com.global.api.entities.billing.enums;

import com.global.api.entities.enums.IStringConstant;

public enum RecurringAuthorizationType implements IStringConstant {
    UNASSIGNED("Unassigned"),
    SIGNED_CONTRACT_INPLACE("SignedContractInPlace"),
    NEED_TO_PRINT_CONTRACT("NeedToPrintContract"),
    RECORDED_CALL_INPLACE("RecordedCallInPlace");

    String value;

    RecurringAuthorizationType(String value) {
        this.value = value;
    }

    @Override
    public byte[] getBytes() {
        return this.value.getBytes();
    }

    @Override
    public String getValue() {
        return value;
    }
}
