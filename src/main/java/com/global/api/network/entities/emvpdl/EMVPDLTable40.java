package com.global.api.network.entities.emvpdl;

import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class EMVPDLTable40 implements IEMVPDLTable {

    @Getter
    @Setter
    private Integer emvPdlAidCount;

    @Getter
    @Setter
    private List<EmvPdlContactApplicationIdentifier> emvPdlAid;

    @Override
    public <T extends IEMVPDLTable> EMVPDLTable<T> parseData(StringParser stringParser) {
        this.setEmvPdlAidCount(stringParser.readInt(2));
        List<EmvPdlContactApplicationIdentifier> applicationIdentifiers = new ArrayList<>();
        for (int index = 0; index < this.getEmvPdlAidCount(); index++) {
            EmvPdlContactApplicationIdentifier identifier = new EmvPdlContactApplicationIdentifier();
            identifier.setEmvPdlApplicationIdentifier(stringParser.readString(32));
            identifier.setEmvPdlApplicationSelectionIndicator(stringParser.readInt(1));
            identifier.setEmvPdlApplicationVersionNumber(stringParser.readString(4));
            identifier.setEmvPdlApplicationCountryCode(stringParser.readInt(3));
            identifier.setEmvPdlTransactionTypes(stringParser.readString(4));
            identifier.setEmvPdlTerminalCapabilities(stringParser.readString(6));
            identifier.setEmvPdlTerminalFloorLimit(stringParser.readInt(12));
            identifier.setEmvPdlThresholdValueForBiasedRandomSelection(stringParser.readInt(12));
            identifier.setEmvPdlTargetPercentageToBeUsedForRandomSelection(stringParser.readInt(2));
            identifier.setEmvPdlMaximumTargetPercentageToBeUsedForBiasedRandomSelection(stringParser.readInt(2));
            identifier.setTacDenial(stringParser.readString(10));
            identifier.setTacOnline(stringParser.readString(10));
            identifier.setTacDefault(stringParser.readString(10));
            identifier.setEmvPdlTerminalRiskManagementData(stringParser.readString(16));
            identifier.setEmvPdlDefaultTransactionCertificateDataObjectList(stringParser.readString(32));
            identifier.setEmvPdlDefaultDynamicDataAuthenticationDataObjectList(stringParser.readString(32));

            applicationIdentifiers.add(identifier);
        }
        this.setEmvPdlAid(applicationIdentifiers);
        return new EMVPDLTable(this);
    }

    @ToString
    public class EmvPdlContactApplicationIdentifier {
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
        private Integer emvPdlApplicationCountryCode;
        @Getter
        @Setter
        private String emvPdlTransactionTypes;
        @Getter
        @Setter
        private String emvPdlTerminalCapabilities;
        @Getter
        @Setter
        private Integer emvPdlTerminalFloorLimit;
        @Getter
        @Setter
        private Integer emvPdlThresholdValueForBiasedRandomSelection;
        @Getter
        @Setter
        private Integer emvPdlTargetPercentageToBeUsedForRandomSelection;
        @Getter
        @Setter
        private Integer emvPdlMaximumTargetPercentageToBeUsedForBiasedRandomSelection;
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
        private String emvPdlTerminalRiskManagementData;
        @Getter
        @Setter
        private String emvPdlDefaultTransactionCertificateDataObjectList;
        @Getter
        @Setter
        private String emvPdlDefaultDynamicDataAuthenticationDataObjectList;
    }
}
