package com.global.api.network.entities.emvpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class EMVPDLTable30 implements IEMVPDLTable {
    @Getter
    @Setter
    private Integer emvPdlTerminalType;
    @Getter
    @Setter
    private String emvPdlAdditionalTerminalCapabilities;
    @Getter
    @Setter
    private Integer emvPdlTerminalCountryCode;
    @Getter
    @Setter
    private Integer emvPdlTransactionCurrencyCode;
    @Getter
    @Setter
    private Integer emvPdlTransactionCurrencyExponent;
    @Getter
    @Setter
    private Integer emvPdlTransactionReferenceCurrencyCode;
    @Getter
    @Setter
    private Integer emvPdlTransactionReferenceCurrencyExponent;

    @Override
    public EMVPDLTable parseData(StringParser stringParser) {
        this.setEmvPdlTerminalType(stringParser.readInt(2));
        this.setEmvPdlAdditionalTerminalCapabilities(stringParser.readString(10));
        this.setEmvPdlTerminalCountryCode(stringParser.readInt(3));
        this.setEmvPdlTransactionCurrencyCode(stringParser.readInt(3));
        this.setEmvPdlTransactionCurrencyExponent(stringParser.readInt(1));
        this.setEmvPdlTransactionReferenceCurrencyCode(stringParser.readInt(3));
        this.setEmvPdlTransactionReferenceCurrencyExponent(stringParser.readInt(1));
        return new EMVPDLTable(this);
    }

}
