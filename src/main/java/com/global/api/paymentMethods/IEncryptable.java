package com.global.api.paymentMethods;

import com.global.api.entities.EncryptionData;

public interface IEncryptable {
    EncryptionData getEncryptionData();
    void setEncryptionData(EncryptionData encryptionData);
}
