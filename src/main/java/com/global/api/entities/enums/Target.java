package com.global.api.entities.enums;

public enum Target implements IFlag {
    DEFAULT, // Used to refer to the default (most common) values across Connectors
    NWS,
    VAPS,
    Transit,
    Portico,
    Realex,
    GP_API,
    GNAP,
    NTS;

    public long getLongValue() {
        return 1 << this.ordinal();
    }
}
