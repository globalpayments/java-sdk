package com.global.api.network.entities.emvpdl;

import com.global.api.network.enums.nts.EmvPDLCardType;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class EMVPDLTable10V2 implements IEMVPDLTable {

    @Getter
    @Setter
    private Integer emvTableDataBlockLength;
    @Getter
    @Setter
    private Integer emvCdlConfigurationCount;
    @Getter
    @Setter
    private List<TableVersionsFlags> tableVersionsFlags;

    @Override
    public EMVPDLTable parseData(StringParser stringParser) {
        this.setEmvTableDataBlockLength(stringParser.readInt(3));
        this.setEmvCdlConfigurationCount(stringParser.readInt(2));
        List<TableVersionsFlags> versionsFlags = new ArrayList<>();
        for (int versionIndex = 0; versionIndex < this.getEmvCdlConfigurationCount(); versionIndex++) {
            TableVersionsFlags tableVersionsFlag = new TableVersionsFlags();
            tableVersionsFlag.setEmvPdlConfigurationName(stringParser.readString(40));
            tableVersionsFlag.setEmvPdlEnabled(stringParser.readString(1));
            tableVersionsFlag.setEmvPdlTableId30Version(stringParser.readString(3));
            tableVersionsFlag.setEmvPdlTableId30Flag(stringParser.readString(1));
            tableVersionsFlag.setEmvPdlNoOfCardTypes(stringParser.readInt(2));
            List<EMVPDLCardTypesTable> cardTypes = new ArrayList<>();
            for (int index = 0; index < tableVersionsFlag.getEmvPdlNoOfCardTypes(); index++) {
                EMVPDLCardTypesTable card = new EMVPDLCardTypesTable();
                card.setEmvPdlCardType(stringParser.readStringConstant(2, EmvPDLCardType.class));
                card.setEmvPdlTableId40Version(stringParser.readString(3));
                card.setEmvPdlTableId40Flag(stringParser.readString(1));
                card.setEmvPdlTableId50Version(stringParser.readString(3));
                card.setEmvPdlTableId50Flag(stringParser.readString(1));
                card.setEmvPdlTableId60Version(stringParser.readString(3));
                card.setEmvPdlTableId60Flag(stringParser.readString(1));
                cardTypes.add(card);
            }
            tableVersionsFlag.setEmvPdlCardTypes(cardTypes);
            versionsFlags.add(tableVersionsFlag);
        }
        this.setTableVersionsFlags(versionsFlags);

        return new EMVPDLTable(this);
    }

    @ToString
    public class TableVersionsFlags {
        @Getter
        @Setter
        private String emvPdlConfigurationName;
        @Getter
        @Setter
        private String emvPdlEnabled;
        @Getter
        @Setter
        private String emvPdlTableId30Version;
        @Getter
        @Setter
        private String emvPdlTableId30Flag;
        @Getter
        @Setter
        private Integer emvPdlNoOfCardTypes;
        @Getter
        @Setter
        private List<EMVPDLCardTypesTable> emvPdlCardTypes;
    }
}
