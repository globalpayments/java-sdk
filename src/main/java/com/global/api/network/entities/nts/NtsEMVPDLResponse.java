package com.global.api.network.entities.nts;

import com.global.api.network.entities.emvpdl.EMVPDLTable;
import com.global.api.network.entities.emvpdl.EMVPDLTable10;
import com.global.api.network.entities.emvpdl.IEMVPDLTable;
import com.global.api.network.enums.nts.EmvPDLCardType;
import com.global.api.network.enums.nts.PDLEndOfTableFlag;
import com.global.api.network.enums.nts.PDLTableID;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsEMVPDLResponse implements INtsResponseMessage {

    @Getter
    @Setter
    private Boolean isPdlV2;
    @Getter
    @Setter
    private String emvPdlResponseCode;
    @Getter
    @Setter
    private String emvPdlStatusCode;
    @Getter
    @Setter
    private String emvPdlParameterVersion;
    @Getter
    @Setter
    private Integer emvPdlBlockSequenceNumber;
    @Getter
    @Setter
    private PDLTableID emvPdlTableId;
    @Getter
    @Setter
    private EmvPDLCardType emvPdlCardType;
    @Getter
    @Setter
    private PDLEndOfTableFlag emvPdlEndOfTableFlag;
    @Getter
    @Setter
    private String emvPdlConfigurationName;
    @Getter
    @Setter
    private EMVPDLTable table;
    @Getter
    @Setter
    private String emvPdlTableDataBlockLength;
    @Getter
    @Setter
    private String emvPdlTableDataBlockData;

    public NtsEMVPDLResponse(Boolean isPdlV2) {
        this.isPdlV2 = isPdlV2;
    }

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsEMVPDLResponse pdlResponse = new NtsEMVPDLResponse(this.isPdlV2);
        StringParser sp = new StringParser(buffer);

        // Common fields
        pdlResponse.setEmvPdlResponseCode(sp.readString(2));
        pdlResponse.setEmvPdlStatusCode(sp.readString(2));
        pdlResponse.setEmvPdlParameterVersion(sp.readString(3));
        if (this.isPdlV2) {
            pdlResponse.setEmvPdlConfigurationName(sp.readString(40));
        }
        pdlResponse.setEmvPdlBlockSequenceNumber(sp.readInt(2));
        pdlResponse.setEmvPdlTableId(sp.readStringConstant(2, PDLTableID.class));
        pdlResponse.setEmvPdlCardType(sp.readStringConstant(2, EmvPDLCardType.class));
        pdlResponse.setEmvPdlEndOfTableFlag(sp.readStringConstant(1, PDLEndOfTableFlag.class));
        if (!pdlResponse.getEmvPdlEndOfTableFlag().equals(PDLEndOfTableFlag.DownloadConfirmation)) {
            if (pdlResponse.getEmvPdlTableId().equals(PDLTableID.Table10)) {
                if (this.isPdlV2) {
                    pdlResponse.setEmvPdlTableDataBlockData(sp.readRemaining());
                } else {
                    IEMVPDLTable table10 = new EMVPDLTable10();
                    pdlResponse.setTable(table10.parseData(sp));
                }
            } else {
                pdlResponse.setEmvPdlTableDataBlockLength(sp.readString(3));
                pdlResponse.setEmvPdlTableDataBlockData(sp.readRemaining());
            }
        }

        return pdlResponse;
    }
}
