package com.global.api.network.entities;

import com.global.api.network.enums.nts.EmvPDLCardType;
import com.global.api.network.enums.nts.PDLParameterType;
import com.global.api.network.enums.nts.PDLTableID;
import lombok.Getter;
import lombok.Setter;

public class NtsPDLData {

    @Getter
    @Setter
    private PDLParameterType parameterType;

    @Getter
    @Setter
    private String parameterVersion;

    @Getter
    @Setter
    private String blockSequenceNumber;

    @Getter
    @Setter
    private PDLTableID tableId;

    @Getter
    @Setter
    private EmvPDLCardType emvPDLCardType;

    @Getter
    @Setter
    private String emvPdlConfigurationName;

    public Boolean isEMVPDLParameterVersion002() {
        return emvPdlConfigurationName != null;
    }
}
