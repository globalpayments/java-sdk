package com.global.api.network.entities.nts;

import com.global.api.network.enums.*;
import lombok.Getter;
import lombok.Setter;

public class Nts3DESAndTokenizationDataEntry {

    private Nts3DESAndTokenizationDataTag tag;
    @Getter
    @Setter
    private RecordId recordId;
    private String recordType;
    private String keyBlockDataType;
    private String encryptedFieldMatrix;
    private EncryptionType tepType;
    private String cardSecurityCode;
    private String etbBlock;
    private String entryData;
    @Getter @Setter
    private ServiceType serviceType;
    @Getter @Setter
    private OperationType operationType;
    @Getter @Setter
    private String serviceCodeOrigin;
    @Getter @Setter
    private String serviceResponseCode;
    @Getter @Setter
    private String ksn;
    @Getter @Setter
    private String encryptedData;
    @Getter @Setter
    private String tokenizationType;
    @Getter @Setter
    private String tokenizedFieldMatrix;
    @Getter @Setter
    private String tokenizationOperationType;
    @Getter @Setter
    private String merchantId;
    @Getter @Setter
    private String tokenOrAcctNum;
    @Getter @Setter
    private String expiryDate;
    @Getter @Setter
    private boolean isFileAction;

    public Nts3DESAndTokenizationDataTag getTag() {
        return tag;
    }
    public void setTag(Nts3DESAndTokenizationDataTag tag) {
        this.tag = tag;
    }
    public String getRecordType() {
        return recordType;
    }
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }
    public String getKeyBlockDataType() {
        return keyBlockDataType;
    }
    public void setKeyBlockDataType(String keyBlockDataType) {
        this.keyBlockDataType = keyBlockDataType;
    }
    public String getEncryptedFieldMatrix() {
        return encryptedFieldMatrix;
    }
    public void setEncryptedFieldMatrix(String encryptedFieldMatrix) {
        this.encryptedFieldMatrix = encryptedFieldMatrix;
    }
    public EncryptionType getTepType() {
        return tepType;
    }
    public void setTepType(EncryptionType tepType) {
        this.tepType = tepType;
    }
    public String getCardSecurityCode() {
        return cardSecurityCode;
    }
    public void setCardSecurityCode(String cardSecurityCode) {
        this.cardSecurityCode = cardSecurityCode;
    }
    public String getEtbBlock() {
        return etbBlock;
    }
    public void setEtbBlock(String etbBlock) {
        this.etbBlock = etbBlock;
    }
    public String getEntryData() {
        return entryData;
    }
    public void setEntryData(String entryData) {
        this.entryData = entryData;
    }
}
