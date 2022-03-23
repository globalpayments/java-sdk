package com.global.api.network.entities.emvpdl;

import com.global.api.network.enums.nts.PDLTableID;
import com.global.api.utils.StringParser;
import lombok.NonNull;

public interface IEMVPDLTable {
    static EMVPDLTable parseData(@NonNull String data,@NonNull PDLTableID tableId) {
        if (tableId.equals(PDLTableID.Table10)) {
            IEMVPDLTable table = new EMVPDLTable10V2();
            return table.parseData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table30)) {
            IEMVPDLTable table = new EMVPDLTable30();
            return table.parseData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table40)) {
            IEMVPDLTable table = new EMVPDLTable40();
            return table.parseData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table50)) {
            IEMVPDLTable table = new EMVPDLTable50();
            return table.parseData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table60)) {
            IEMVPDLTable table = new EMVPDLTable60();
            return table.parseData(new StringParser(data));
        }
        return null;
    }

    <T extends IEMVPDLTable> EMVPDLTable<T> parseData(StringParser stringParser);
}
