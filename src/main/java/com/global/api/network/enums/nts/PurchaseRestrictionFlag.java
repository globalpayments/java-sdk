package com.global.api.network.enums.nts;

import com.global.api.entities.enums.INumericConstant;

public enum PurchaseRestrictionFlag implements INumericConstant {
    NoRestriction(0),
    ChipBased(1),
    HostBased(2),
    BothChipAndHostBased(3);

    private final int value;
    PurchaseRestrictionFlag(int value){
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
