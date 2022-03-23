package com.global.api.network.entities.emvpdl;

import com.global.api.network.enums.nts.EmvPDLCardType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class EMVPDLCardTypesTable {
    @Getter
    @Setter
    private EmvPDLCardType emvPdlCardType;
    @Getter
    @Setter
    private String emvPdlTableId40Version;
    @Getter
    @Setter
    private String emvPdlTableId40Flag;
    @Getter
    @Setter
    private String emvPdlTableId50Version;
    @Getter
    @Setter
    private String emvPdlTableId50Flag;
    @Getter
    @Setter
    private String emvPdlTableId60Version;
    @Getter
    @Setter
    private String emvPdlTableId60Flag;

}
