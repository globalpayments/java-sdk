package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE127_ForwardingDataTag;
import com.global.api.network.enums.EncryptionType;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

import java.util.LinkedList;

public class DE127_ForwardingData implements IDataElement<DE127_ForwardingData> {
    private LinkedList<DE127_ForwardingDataEntry> entries;

    private int getEntryCount() {
        return entries.size();
    }

    public DE127_ForwardingData() {
        entries = new LinkedList<DE127_ForwardingDataEntry>();
    }

    public void addEncryptionData(EncryptionType encryptionType, String ktb) {
        addEncryptionData(encryptionType, ktb, null);
    }
    public void addEncryptionData(EncryptionType encryptionType, String ktb, String cvn) {
        if(cvn == null) {
            cvn = "       ";
        }

        DE127_ForwardingDataEntry entry = new DE127_ForwardingDataEntry();
        entry.setTag(DE127_ForwardingDataTag.E3_EncryptedData);
        entry.setRecordId("E3");
        entry.setRecordType("001");
        entry.setKeyBlockDataType("v");
        entry.setEncryptedFieldMatrix(StringUtils.isNullOrEmpty(cvn) ? "03" : "04");
        entry.setTepType(encryptionType);
        entry.setCardSecurityCode(cvn);
        entry.setEtbBlock(ktb);

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

                    entry.setRecordId(ed.readString(2));
                    entry.setRecordType(ed.readString(3));
                    entry.setKeyBlockDataType(ed.readString(1));
                    entry.setEncryptedFieldMatrix(ed.readString(2));
                    entry.setTepType(ed.readStringConstant(1, EncryptionType.class));
                    ed.readString(18); // reserved
                    entry.setCardSecurityCode(ed.readString(7));
                    ed.readString(45); // reserved
                    entry.setEtbBlock(ed.readLLLVAR());
                } break;
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
                    String entryData = entry.getRecordId()
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
