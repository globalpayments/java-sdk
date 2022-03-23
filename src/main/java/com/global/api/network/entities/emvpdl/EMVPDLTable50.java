package com.global.api.network.entities.emvpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class EMVPDLTable50 implements IEMVPDLTable {
    @Getter
    @Setter
    private Integer emvPdlAidCount;

    @Getter
    @Setter
    private List<EmvPdlContactLessApplicationIdentifier> emvPdlAid;

    @Override
    public <T extends IEMVPDLTable> EMVPDLTable<T> parseData(StringParser stringParser) {
        this.setEmvPdlAidCount(stringParser.readInt(2));
        List<EmvPdlContactLessApplicationIdentifier> applicationIdentifiers = new ArrayList<>();
        for (int index = 0; index < this.getEmvPdlAidCount(); index++) {
            EmvPdlContactLessApplicationIdentifier identifier = new EmvPdlContactLessApplicationIdentifier();
            identifier.setEmvPdlApplicationIdentifier(stringParser.readString(32));
            identifier.setEmvPdlApplicationSelectionIndicator(stringParser.readInt(1));
            identifier.setEmvPdlApplicationVersionNumber(stringParser.readString(4));
            identifier.setEmvPdlMagstripeApplicationVersionNumber(stringParser.readString(4));
            identifier.setEmvPdlApplicationCountryCode(stringParser.readInt(3));
            identifier.setEmvPdlTransactionTypes(stringParser.readString(4));
            identifier.setEmvPdlTerminalCapabilities(stringParser.readString(6));
            identifier.setEmvPdlTerminalContactlessFloorLimit(stringParser.readInt(12));
            identifier.setEmvPdlTerminalCvmRequiredLimit(stringParser.readInt(12));
            identifier.setEmvPdlTerminalContactlessTransactionLimit(stringParser.readInt(12));
            identifier.setTacDenial(stringParser.readString(10));
            identifier.setTacOnline(stringParser.readString(10));
            identifier.setTacDefault(stringParser.readString(10));
            identifier.setEmvPdlTerminalTransactionQualifiers(stringParser.readString(8));
            identifier.setEmvPdlTerminalRiskManagementData(stringParser.readString(16));
            identifier.setEmvPdlDefaultTransactionCertificateDataObjectList(stringParser.readString(32));
            applicationIdentifiers.add(identifier);
        }
        this.setEmvPdlAid(applicationIdentifiers);
        return new EMVPDLTable(this);
    }

    @ToString
    public class EmvPdlContactLessApplicationIdentifier {
        @Getter
        @Setter
        private String emvPdlApplicationIdentifier;
        @Getter
        @Setter
        private Integer emvPdlApplicationSelectionIndicator;
        @Getter
        @Setter
        private String emvPdlApplicationVersionNumber;
        @Getter
        @Setter
        private String emvPdlMagstripeApplicationVersionNumber;
        @Getter
        @Setter
        private Integer emvPdlApplicationCountryCode;
        @Getter
        @Setter
        private String emvPdlTransactionTypes;
        @Getter
        @Setter
        private String emvPdlTerminalCapabilities;
        @Getter
        @Setter
        private Integer emvPdlTerminalContactlessFloorLimit;
        @Getter
        @Setter
        private Integer emvPdlTerminalCvmRequiredLimit;
        @Getter
        @Setter
        private Integer emvPdlTerminalContactlessTransactionLimit;
        @Getter
        @Setter
        private String tacDenial;
        @Getter
        @Setter
        private String tacOnline;
        @Getter
        @Setter
        private String tacDefault;
        @Getter
        @Setter
        private String emvPdlTerminalTransactionQualifiers;
        @Getter
        @Setter
        private String emvPdlTerminalRiskManagementData;
        @Getter
        @Setter
        private String emvPdlDefaultTransactionCertificateDataObjectList;
    }
}
