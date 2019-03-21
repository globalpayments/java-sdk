package com.global.api.network.elements;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.utils.StringParser;

public class DE72_DataRecord implements IDataElement<DE72_DataRecord> {
    public DE72_DataRecord fromByteArray(byte[] buffer) {
        StringParser sp = new StringParser(buffer);

        // TODO: Parse out this element

        return this;
    }

    public byte[] toByteArray() {
        String rvalue = "";

        return rvalue.getBytes();
    }

    public String toString() {
        return new String(toByteArray());
    }
}
