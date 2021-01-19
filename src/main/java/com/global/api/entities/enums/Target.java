package com.global.api.entities.enums;

public enum Target implements IFlag {
    NWS,
    VAPS,
    Transit,
    Portico,
    Realex,
    GP_API;

    public long getLongValue() {
        return 1 << this.ordinal();
    }
}
