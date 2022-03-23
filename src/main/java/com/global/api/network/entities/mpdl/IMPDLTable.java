package com.global.api.network.entities.mpdl;

import com.global.api.network.enums.nts.PDLTableID;
import com.global.api.utils.StringParser;
import lombok.NonNull;

public interface IMPDLTable {

    static <T extends IMPDLTable> MPDLTable<T> parseData(@NonNull String data, @NonNull PDLTableID tableId) {
        if (tableId.equals(PDLTableID.Table30)) {
            MPDLTable30 table = new MPDLTable30();
            return table.parseTableData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table40)) {
            MPDLTable40 table = new MPDLTable40();
            return table.parseTableData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table50)) {
            MPDLTable50 table = new MPDLTable50();
            return table.parseTableData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table60)) {
            MPDLTable60 table = new MPDLTable60();
            return table.parseTableData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table70)) {
            MPDLTable70 table = new MPDLTable70();
            return table.parseTableData(new StringParser(data));
        } else if (tableId.equals(PDLTableID.Table80)) {
            MPDLTable80 table = new MPDLTable80();
            return table.parseTableData(new StringParser(data));
        }
        return null;
    }

    <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp);
}
