package com.global.api.network.entities.mpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class MPDLTable80 implements IMPDLTable {
    @Getter
    @Setter
    private Integer responseCodeCount;
    @Getter
    @Setter
    private List<NtsResponseCode> ntsResponseCodes;

    @Override
    public <T extends IMPDLTable> MPDLTable<T> parseTableData(StringParser sp) {
        this.setResponseCodeCount(sp.readInt(2));
        this.ntsResponseCodes = new ArrayList<>();
        for (int index = 0; index < this.getResponseCodeCount(); index++) {
            NtsResponseCode ntsResponseCode = new NtsResponseCode();
            ntsResponseCode.setNtsCode(sp.readInt(2));
            ntsResponseCode.setResponseMessage(sp.readString(20));
            ntsResponseCode.setPumpMessageNormal(sp.readString(20));
            ntsResponseCode.setPumpMessageUnattended(sp.readString(20));
            this.ntsResponseCodes.add(ntsResponseCode);
        }
        return new MPDLTable(this);
    }

    @ToString
    public class NtsResponseCode {
        @Getter
        @Setter
        private Integer ntsCode;
        @Getter
        @Setter
        private String responseMessage;
        @Getter
        @Setter
        private String pumpMessageNormal;
        @Getter
        @Setter
        private String pumpMessageUnattended;
    }
}
