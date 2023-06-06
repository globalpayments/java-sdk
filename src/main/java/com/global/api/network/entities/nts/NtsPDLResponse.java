package com.global.api.network.entities.nts;

import com.global.api.network.entities.mpdl.IMPDLTable;
import com.global.api.network.entities.mpdl.MPDLTable;
import com.global.api.network.entities.mpdl.MPDLTable10;
import com.global.api.network.enums.nts.PDLEndOfTableFlag;
import com.global.api.network.enums.nts.PDLTableID;
import com.global.api.utils.NtsUtils;
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
        NtsUtils.log("Parameter or Table Version ", pdlResponse.getParameterVersionOrTableVersion());

        pdlResponse.setBlockSequenceNumber(sp.readInt(2));
        NtsUtils.log("Block Sequence Number", pdlResponse.getBlockSequenceNumber());

        pdlResponse.setTableId(sp.readStringConstant(2, PDLTableID.class));
        NtsUtils.log("Table ID ", pdlResponse.getTableId());

        pdlResponse.setEndOfTableFlag(sp.readStringConstant(1, PDLEndOfTableFlag.class));
        NtsUtils.log("End Of table flag", pdlResponse.getEndOfTableFlag());
        if(pdlResponse.getTableId()!= null){
            if (pdlResponse.getTableId().equals(PDLTableID.Table10)) {
                IMPDLTable table10 = new MPDLTable10();
                pdlResponse.setTable(table10.parseTableData(sp));
            } else {
                pdlResponse.setTableDataBlockLength(sp.readString(3));
                pdlResponse.setTableDataBlockData(sp.readRemaining());
            }
        }
        return pdlResponse;
    }
}
