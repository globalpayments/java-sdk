package com.global.api.network.elements;

import com.global.api.entities.EncryptionData;
import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.*;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;

public class DE127_ForwardingData implements IDataElement<DE127_ForwardingData> {
    private LinkedList<DE127_ForwardingDataEntry> entries;

    private int getEntryCount() {
        return entries.size();
    }

    public DE127_ForwardingData() {
        entries = new LinkedList<DE127_ForwardingDataEntry>();
    }
    @Setter @Getter
    private ServiceType serviceType;
    @Setter @Getter
    private OperationType operationType=OperationType.Reserved;
    @Setter @Getter
    private EncryptedFieldMatrix encryptedField;

    public void addEncryptionData(EncryptionType encryptionType,EncryptionData encryptionData) {
        addEncryptionData(encryptionType,encryptionData,null);
    }

    public void addEncryptionData(EncryptionType encryptionType,EncryptionData encryptionData,String cvn) {

        DE127_ForwardingDataEntry entry = new DE127_ForwardingDataEntry();
        String ktb=encryptionData.getKtb();
        switch(encryptionType) {
            case TEP1:
            case TEP2:
                entry.setTag(DE127_ForwardingDataTag.E3_EncryptedData) ;
                entry.setRecordId(RecordId.E3_Encryption);
                entry.setRecordType("001");
                entry.setKeyBlockDataType("v");
                entry.setEncryptedFieldMatrix(encryptedField.getValue());
                entry.setTepType(encryptionType);
                entry.setCardSecurityCode(cvn!=null?cvn:StringUtils.padRight("",7,' '));
                entry.setEtbBlock(ktb);
                break;
            case TDES:
                String ksn=encryptionData.getKsn();
                entry.setTag(DE127_ForwardingDataTag.Encryption_3DES) ;
                entry.setRecordId(RecordId.Encryption_3DE);
                entry.setRecordType("001");
                entry.setServiceType(serviceType);
                entry.setTepType(encryptionType);
                entry.setEncryptedFieldMatrix(encryptedField.getValue());
                entry.setOperationType(operationType);
                entry.setKsn(StringUtils.padRight(ksn,24,' '));
                entry.setEncryptedData(ktb);
        }

        add(entry);
    }
    public void add(DE127_ForwardingDataEntry entry) {
        entries.clear();
        entries.add(entry);
    }

    public DE127_ForwardingData fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        int entryCount = sp.readInt(2);
        for(int i = 0; i < entryCount; i++) {
            DE127_ForwardingDataEntry entry = new DE127_ForwardingDataEntry();
            entry.setTag(sp.readStringConstant(3, DE127_ForwardingDataTag.class));

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
                default: {
                    entry.setEntryData(data);
                }
            }

            entries.add(entry);
        }

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = StringUtils.padLeft(getEntryCount(), 2, '0');

        for(DE127_ForwardingDataEntry entry: entries) {
            rvalue = rvalue.concat(entry.getTag().getValue());

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
