package com.global.api.network.entities.emvpdl;

import lombok.Getter;
import lombok.Setter;

public class EMVPDLTable <T extends IEMVPDLTable>{
    @Getter
    @Setter
    private T table;

    public EMVPDLTable(T table) {
        this.table = table;
    }
}
