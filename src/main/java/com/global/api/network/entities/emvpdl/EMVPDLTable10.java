package com.global.api.network.entities.emvpdl;

import com.global.api.network.enums.nts.EmvPDLCardType;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ToString
public class EMVPDLTable10 implements IEMVPDLTable {
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

    @Override
    public EMVPDLTable parseData(StringParser stringParser) {
        this.setEmvPdlEnabled(stringParser.readString(1));
        this.setEmvPdlTableId30Version(stringParser.readString(3));
        this.setEmvPdlTableId30Flag(stringParser.readString(1));
        this.setEmvPdlNoOfCardTypes(stringParser.readInt(2));
        List<EMVPDLCardTypesTable> cardTypes = new ArrayList<>();
        for (int index = 0; index < this.getEmvPdlNoOfCardTypes(); index++) {
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
        this.setEmvPdlCardTypes(cardTypes);

        return new EMVPDLTable(this);
    }

    public Optional<EMVPDLCardTypesTable> getTableVersionByCardType(EmvPDLCardType emvPDLCardType){
        return emvPdlCardTypes.stream().filter(card -> card.getEmvPdlCardType().equals(emvPDLCardType)).findFirst();
    }
}
