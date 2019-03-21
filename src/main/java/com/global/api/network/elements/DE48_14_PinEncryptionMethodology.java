package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE48_EncryptionAlgorithmDataCode;
import com.global.api.network.enums.DE48_KeyManagementDataCode;
import com.global.api.utils.StringParser;

public class DE48_14_PinEncryptionMethodology implements IDataElement<DE48_14_PinEncryptionMethodology> {
    private DE48_KeyManagementDataCode keyManagementDataCode;
    private DE48_EncryptionAlgorithmDataCode encryptionAlgorithmDataCode;

    public DE48_KeyManagementDataCode getKeyManagementDataCode() {
        return keyManagementDataCode;
    }
    public void setKeyManagementDataCode(DE48_KeyManagementDataCode keyManagementDataCode) {
        this.keyManagementDataCode = keyManagementDataCode;
    }
    public DE48_EncryptionAlgorithmDataCode getEncryptionAlgorithmDataCode() {
        return encryptionAlgorithmDataCode;
    }
    public void setEncryptionAlgorithmDataCode(DE48_EncryptionAlgorithmDataCode encryptionAlgorithmDataCode) {
        this.encryptionAlgorithmDataCode = encryptionAlgorithmDataCode;
    }

    public DE48_14_PinEncryptionMethodology fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        keyManagementDataCode = sp.readStringConstant(1, DE48_KeyManagementDataCode.class);
        encryptionAlgorithmDataCode = sp.readStringConstant(1, DE48_EncryptionAlgorithmDataCode.class);

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = keyManagementDataCode.getValue()
                .concat(encryptionAlgorithmDataCode.getValue());
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
