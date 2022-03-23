package com.global.api.network.entities.nts;

import com.global.api.network.entities.mpdl.IMPDLTable;
import com.global.api.network.entities.mpdl.MPDLTable;
import com.global.api.network.entities.mpdl.MPDLTable10;
import com.global.api.network.enums.nts.PDLEndOfTableFlag;
import com.global.api.network.enums.nts.PDLTableID;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsPDLResponse implements INtsResponseMessage {

    @Getter
    @Setter
    private String parameterVersionOrTableVersion;
    @Getter
    @Setter
    private Integer blockSequenceNumber;
    @Getter
    @Setter
    private PDLTableID tableId;
    @Getter
    @Setter
    private PDLEndOfTableFlag endOfTableFlag;
    @Getter
    @Setter
    private String tableDataBlockLength;
    @Getter
    @Setter
    private MPDLTable table;
    @Getter
    @Setter
    private String tableDataBlockData;

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsPDLResponse pdlResponse = new NtsPDLResponse();
        StringParser sp = new StringParser(buffer);

        // Common fields
        pdlResponse.setParameterVersionOrTableVersion(sp.readString(3));
        pdlResponse.setBlockSequenceNumber(sp.readInt(2));
        pdlResponse.setTableId(sp.readStringConstant(2, PDLTableID.class));
        pdlResponse.setEndOfTableFlag(sp.readStringConstant(1, PDLEndOfTableFlag.class));

        if (pdlResponse.getTableId().equals(PDLTableID.Table10)) {
            IMPDLTable table10 = new MPDLTable10();
            pdlResponse.setTable(table10.parseTableData(sp));
        } else {
            pdlResponse.setTableDataBlockLength(sp.readString(3));
            pdlResponse.setTableDataBlockData(sp.readRemaining());
        }
        return pdlResponse;
    }
}
