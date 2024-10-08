package com.global.api.network.entities.nts;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

public class Nts3DESAndTokenizationData implements IDataElement<Nts3DESAndTokenizationData> {
    private LinkedList<Nts3DESAndTokenizationDataEntry> entries;
    private int getEntryCount() {
        return entries.size();
    }
    public Nts3DESAndTokenizationData() {
        entries = new LinkedList<>();
    }
    @Setter @Getter
    private ServiceType serviceType;
    @Setter @Getter
    private OperationType operationType=OperationType.Reserved;
    @Setter @Getter
    private EncryptedFieldMatrix encryptedField;
    @Getter @Setter
    private TokenizationType tokenizationType;
    @Getter @Setter
    private TokenizedFieldMatrix tokenizedFieldMatrix;
    @Getter @Setter
    private TokenizationOperationType tokenizationOperationType;
    @Getter @Setter
    private String merchantId;
    @Getter @Setter
    private String tokenOrAcctNum;
    @Getter @Setter
    private String expiryDate;

    public void addNtsTokenizationData(TokenizationType tokenizationType) {
        Nts3DESAndTokenizationDataEntry entry = new Nts3DESAndTokenizationDataEntry();
        entry.setTag(Nts3DESAndTokenizationDataTag.Tokenization_TOK) ;
        entry.setRecordId(RecordId.Tokenization_TD);
        entry.setRecordType("001");
        entry.setServiceType(serviceType);
        entry.setTokenizationType(tokenizationType.getValue());
        entry.setTokenizedFieldMatrix(tokenizedFieldMatrix.getValue());
        entry.setTokenizationOperationType(tokenizationOperationType.getValue());
        entry.setMerchantId(StringUtils.padRight(merchantId,32,' '));
        entry.setTokenOrAcctNum(StringUtils.padRight(tokenOrAcctNum,128,' '));
        entry.setExpiryDate(expiryDate!=null?expiryDate:StringUtils.padRight("",4,' '));
        add(entry);
    }

    public void add(Nts3DESAndTokenizationDataEntry entry) {
        entries.clear();
        entries.add(entry);
    }

    public Nts3DESAndTokenizationData fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);
        int entryCount = sp.readInt(2);
        for(int i = 0; i < entryCount; i++) {
            Nts3DESAndTokenizationDataEntry entry = new Nts3DESAndTokenizationDataEntry();
            entry.setTag(sp.readStringConstant(3, Nts3DESAndTokenizationDataTag.class));

            String data = sp.readLLLVAR();
            switch (entry.getTag()) {
                case E3_EncryptedData: {
                    StringParser ed = new StringParser(data.getBytes());
                    entry.setRecordId(ed.readStringConstant(2,RecordId.class));
                    entry.setRecordType(ed.readString(3));
                    entry.setKeyBlockDataType(ed.readString(1));
                    entry.setEncryptedFieldMatrix(ed.readString(2));
                    entry.setTepType(ed.readStringConstant(1, EncryptionType.class));
                    ed.readString(18); // reserved
                    entry.setCardSecurityCode(ed.readString(7));
                    ed.readString(45); // reserved
                    entry.setEtbBlock(ed.readLLLVAR());
                } break;
                case Encryption_3DES:{
                    StringParser ed = new StringParser(data.getBytes());
                    entry.setRecordId(ed.readStringConstant(2,RecordId.class));
                    entry.setRecordType(ed.readString(3));
                    entry.setServiceType(ed.readStringConstant(1,ServiceType.class));
                    entry.setTepType(ed.readStringConstant(1, EncryptionType.class));
                    entry.setEncryptedFieldMatrix(ed.readString(1));
                    entry.setOperationType(ed.readStringConstant(1,OperationType.class));
                    entry.setServiceCodeOrigin(ed.readString(2));
                    entry.setServiceResponseCode(ed.readString(3));
                    ed.readString(2); // reserved
                    entry.setKsn(ed.readString(24));
                    ed.readString(8); // reserved
                    entry.setEntryData(ed.readString(256));
                    ed.readString(32); // reserved
                }break;
                case Tokenization_TOK:{
                    StringParser ed = new StringParser(data.getBytes());
                    entry.setRecordId(ed.readStringConstant(2,RecordId.class));
                    entry.setRecordType(ed.readString(3));
                    entry.setServiceType(ed.readStringConstant(1,ServiceType.class));
                    entry.setTokenizationType(ed.readString(1));
                    entry.setTokenizedFieldMatrix(ed.readString(1));
                    entry.setTokenizationOperationType(ed.readString(1));
                    ed.readString(7);
                    entry.setMerchantId(ed.readString(32));
                    entry.setTokenOrAcctNum(ed.readString(128));
                    entry.setExpiryDate(ed.readString(4));
                    ed.readString(36);

                }break;
                default: {
                    entry.setEntryData(data);
                }
            }
            entries.add(entry);
        }
        return this;
    }

    public byte[] toByteArray() {
        String rvalue = "";
        for(Nts3DESAndTokenizationDataEntry entry: entries) {
            switch (entry.getTag()) {
                case E3_EncryptedData: {
                    String entryData = entry.getRecordId().getValue()
                            .concat(entry.getRecordType())
                            .concat(entry.getKeyBlockDataType())
                            .concat(entry.getEncryptedFieldMatrix())
                            .concat(entry.getTepType().getValue())
                            .concat(StringUtils.padRight("", 18, ' '))
                            .concat(entry.getCardSecurityCode())
                            .concat(StringUtils.padRight("", 45, ' '))
                            .concat(StringUtils.toLLLVar(entry.getEtbBlock()));
                    rvalue = rvalue.concat(StringUtils.toLLLVar(entryData));
                } break;
                case Encryption_3DES: {
                    String entryData = entry.getRecordId().getValue()
                            .concat(entry.getRecordType())
                            .concat(entry.getServiceType().getValue())
                            .concat(entry.getTepType().getValue())
                            .concat(entry.getEncryptedFieldMatrix())
                            .concat(entry.getOperationType().getValue())
                            .concat(entry.getServiceCodeOrigin()!=null?entry.getServiceCodeOrigin():StringUtils.padRight("", 2, ' '))
                            .concat(entry.getServiceResponseCode()!=null?entry.getServiceResponseCode():StringUtils.padRight("", 3, ' '))
                            .concat(StringUtils.padRight("", 2, ' '))
                            .concat(StringUtils.padRight(entry.getKsn(), 24, ' '))
                            .concat(StringUtils.padRight("", 8, ' '))
                            .concat(StringUtils.padRight(entry.getEncryptedData(),256,' '))
                            .concat(StringUtils.padRight("", 32, ' '));
                    rvalue = rvalue.concat(StringUtils.toLLLVar(entryData));
                } break;
                case Tokenization_TOK :{
                    String entryData = entry.getRecordId().getValue()
                            .concat(entry.getRecordType())
                            .concat(entry.getServiceType().getValue())
                            .concat(entry.getTokenizationType())
                            .concat(entry.getTokenizedFieldMatrix())
                            .concat(entry.getTokenizationOperationType())
                            .concat(StringUtils.padRight("", 7, ' '))
                            .concat(StringUtils.padRight(entry.getMerchantId(),32,' '))
                            .concat(StringUtils.padRight(entry.getTokenOrAcctNum(),128,' '))
                            .concat(entry.getExpiryDate()!=null?entry.getExpiryDate():StringUtils.padRight("", 4, ' '))
                            .concat(StringUtils.padRight("", 36, ' '));
                    rvalue = rvalue.concat(entryData);
                }break;
                default: {
                    rvalue = rvalue.concat(StringUtils.toLLLVar(entry.getEntryData()));
                }
            }
        }
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }

}
