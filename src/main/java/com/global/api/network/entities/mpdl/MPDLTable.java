package com.global.api.network.entities.mpdl;

import lombok.Getter;
import lombok.Setter;

public class MPDLTable<T extends IMPDLTable> {
    @Getter
    @Setter
    private T table;

    public MPDLTable(T table) {
        this.table = table;
    }
}
