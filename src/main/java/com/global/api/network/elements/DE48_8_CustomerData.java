package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DE48_CustomerDataType;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class DE48_8_CustomerData implements IDataElement<DE48_8_CustomerData> {
    private int fieldCount;
    private LinkedHashMap<DE48_CustomerDataType, String> fields;
    private boolean emvFlag;

    public int getFieldCount() {
        return fields.size();
    }

    public void setEmvFlag(boolean emvFlag) {
        this.emvFlag = emvFlag;
    }

    public HashMap<DE48_CustomerDataType, String> getFields() {
        return fields;
    }

    public String get(DE48_CustomerDataType type) {
        if(fields.containsKey(type)) {
            return fields.get(type);
        }
        return null;
    }
    public void set(DE48_CustomerDataType type, String value) {
        if(value != null) {
            fields.put(type, value);
        }
    }

    public DE48_8_CustomerData() {
        fields = new LinkedHashMap<DE48_CustomerDataType, String>();
    }

    public DE48_8_CustomerData fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        fieldCount = sp.readInt(2);
        for(int i = 0; i < fieldCount; i++) {
            DE48_CustomerDataType type = sp.readStringConstant(1, DE48_CustomerDataType.class);
            String value = sp.readToChar('\\');
            fields.put(type, value);
        }

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = StringUtils.padLeft(getFieldCount(), 2, '0');
        for(DE48_CustomerDataType type: fields.keySet()) {
            String value = fields.get(type);
            rvalue = rvalue.concat(type.getValue()).concat(value).concat("\\");
        }

        // strip the final '\\'
        rvalue = !emvFlag ? StringUtils.trimEnd(rvalue, "\\") : rvalue;
        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
