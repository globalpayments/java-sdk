package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE124_SundryDataTag;
import com.global.api.utils.ReverseStringEnumMap;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

import java.util.LinkedList;

public class DE124_SundryData implements IDataElement<DE124_SundryData> {
    private int entryCount;
    private LinkedList<DE124_SundryEntry> entries;

    public int getEntryCount() {
        return entryCount;
    }
    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }
    public LinkedList<DE124_SundryEntry> getEntries() {
        return entries;
    }
    public void setEntries(LinkedList<DE124_SundryEntry> entries) {
        this.entries = entries;
    }

    public DE124_SundryData() {
        entries = new LinkedList<DE124_SundryEntry>();
    }

    public DE124_SundryData fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        entryCount = sp.readInt(2);
        for(int i = 0; i < entryCount; i++) {
            DE124_SundryEntry entry = new DE124_SundryEntry();
            entry.setTag(sp.readStringConstant(2, DE124_SundryDataTag.class));

            String data = sp.readLLLVAR();
            switch(entry.getTag()) {
                case ClientSuppliedData: {
                    entry.setCustomerData(data);
                } break;
                case PiggyBack_CollectTransaction: {
                    StringParser ed = new StringParser(data.getBytes());

                    entry.setPrimaryAccountNumber(ed.readLLVAR());
                    entry.setProcessingCode(new DE3_ProcessingCode().fromByteArray(ed.readString(6).getBytes()));
                    entry.setTransactionAmount(StringUtils.toAmount(ed.readString(12)));
                    entry.setSystemTraceAuditNumber(ed.readString(6));
                    entry.setTransactionLocalDateTime(ed.readString(12));
                    entry.setExpirationDate(ed.readString(4));
                    entry.setPosDataCode(new DE22_PosDataCode().fromByteArray(ed.readString(12).getBytes()));
                    entry.setFunctionCode(ed.readString(3));
                    entry.setMessageReasonCode(ed.readString(4));
                    entry.setApprovalCode(ed.readString(6));
                    entry.setBatchNumber(ed.readString(10));
                    entry.setCardType(ed.readString(4));
                    entry.setMessageTypeIndicator(ed.readString(4));
                    entry.setOriginalStan(ed.readString(6));
                    entry.setOriginalDateTime(ed.readString(12));
                    entry.setCardIssuerData(new DE62_CardIssuerData().fromByteArray(ed.readLLLVAR().getBytes()));
                    entry.setProductData(new DE63_ProductData().fromByteArray(ed.readLLLVAR().getBytes()));
                } break;
                case PiggyBack_AuthCaptureData: {
                    StringParser ed = new StringParser(data.getBytes());

                    entry.setSystemTraceAuditNumber(ed.readString(6));
                    entry.setApprovalCode(ed.readString(6));
                    entry.setTransactionAmount(StringUtils.toAmount(ed.readString(12)));
                    entry.setCustomerData(ed.readRemaining());
                } break;
            }

            entries.add(entry);
        }

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = StringUtils.padLeft(entryCount + "", 2, '0');

        for(DE124_SundryEntry entry: entries) {
            rvalue = rvalue.concat(entry.getTag().getValue());
            switch(entry.getTag()) {
                case ClientSuppliedData: {
                    String length = StringUtils.padLeft(entry.getCustomerData().length() + "", 3, '0');
                    rvalue = rvalue.concat(length + entry.getCustomerData());
                } break;
                case PiggyBack_CollectTransaction: {
                    String entryData = StringUtils.toLLVar(entry.getPrimaryAccountNumber())
                            .concat(new String(entry.getProcessingCode().toByteArray()))
                            .concat(StringUtils.toNumeric(entry.getTransactionAmount(), 12))
                            .concat(entry.getSystemTraceAuditNumber())
                            .concat(entry.getTransactionLocalDateTime())
                            .concat(entry.getExpirationDate())
                            .concat(new String(entry.getPosDataCode().toByteArray()))
                            .concat(entry.getFunctionCode())
                            .concat(entry.getMessageReasonCode())
                            .concat(entry.getApprovalCode())
                            .concat(entry.getBatchNumber())
                            .concat(entry.getCardType())
                            .concat(entry.getMessageTypeIndicator())
                            .concat(entry.getOriginalStan())
                            .concat(entry.getOriginalDateTime())
                            .concat(new String(entry.getCardIssuerData().toByteArray()))
                            .concat(new String(entry.getProductData().toByteArray()));

                    rvalue = rvalue.concat(StringUtils.toLLLVar(entryData));
                } break;
                case PiggyBack_AuthCaptureData: {
                    String entryData = entry.getSystemTraceAuditNumber()
                            .concat(entry.getApprovalCode())
                            .concat(StringUtils.toNumeric(entry.getTransactionAmount(), 12))
                            .concat(entry.getCustomerData());

                    rvalue = rvalue.concat(StringUtils.toLLLVar(entryData));
                } break;
            }
        }

        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
