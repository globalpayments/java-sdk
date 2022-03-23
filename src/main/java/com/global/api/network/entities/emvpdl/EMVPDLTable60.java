package com.global.api.network.entities.emvpdl;

import com.global.api.network.enums.nts.EMVPDLKeyStatus;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public class EMVPDLTable60 implements IEMVPDLTable {
    @Getter
    @Setter
    private Integer emvPdlKeyCount;
    @Getter
    @Setter
    private List<EmvPdlKey> emvPdlKeys;

    @Override
    public <T extends IEMVPDLTable> EMVPDLTable<T> parseData(StringParser stringParser) {
        this.setEmvPdlKeyCount(stringParser.readInt(2));
        List<EmvPdlKey> keys = new ArrayList<>();
        for (int index = 0; index < this.getEmvPdlKeyCount(); index++) {
            EmvPdlKey key = new EmvPdlKey();
            key.setEmvPdlRegisteredApplicationProviderIdentifier(stringParser.readString(10));
            key.setEmvPdlCertificationAuthorityPublicKeyIndex(stringParser.readString(2));
            key.setEmvPdlKeyStatus(stringParser.readStringConstant(1, EMVPDLKeyStatus.class));
            if (key.getEmvPdlKeyStatus().equals(EMVPDLKeyStatus.Active)) {
                key.setEmvPdlCertificationAuthorityPublicKeyModulusLength(stringParser.readInt(4));
                key.setEmvPdlCertificationAuthorityPublicKeyModulus(stringParser.readString(key.getEmvPdlCertificationAuthorityPublicKeyModulusLength()));
                key.setEmvPdlCertificationAuthorityPublicKeyExponent(stringParser.readString(2));
                key.setEmvPdlCertificationAuthorityPublicKeyCheckSum(stringParser.readString(40));
            }
            keys.add(key);
        }
        this.setEmvPdlKeys(keys);
        return new EMVPDLTable(this);
    }

    @ToString
    public class EmvPdlKey {
        @Getter
        @Setter
        private String emvPdlRegisteredApplicationProviderIdentifier;
        @Getter
        @Setter
        private String emvPdlCertificationAuthorityPublicKeyIndex;
        @Getter
        @Setter
        private EMVPDLKeyStatus emvPdlKeyStatus;
        @Getter
        @Setter
        private Integer emvPdlCertificationAuthorityPublicKeyModulusLength;
        @Getter
        @Setter
        private String emvPdlCertificationAuthorityPublicKeyModulus;
        @Getter
        @Setter
        private String emvPdlCertificationAuthorityPublicKeyExponent;
        @Getter
        @Setter
        private String emvPdlCertificationAuthorityPublicKeyCheckSum;
    }
}
