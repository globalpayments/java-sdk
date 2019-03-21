package com.global.api.network;

import com.global.api.network.abstractions.IDataElement;
import com.global.api.network.enums.DataElementType;
import com.global.api.network.enums.DataElementId;
import com.global.api.utils.MessageReader;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.StringUtils;

public class Iso8583Element {
    private DataElementId id;
    private DataElementType type;
    private String description;
    private Integer length;
    private byte[] buffer;

    public DataElementId getId() {
        return id;
    }
    public DataElementType getType() {
        return type;
    }
    public String getDescription() {
        return description;
    }
    public Integer getLength() {
        return length;
    }
    public byte[] getBuffer() {
        return buffer;
    }
    byte[] getSendBuffer() {
        if(buffer == null) {
            return new byte[0];
        }

        switch (type) {
            case LVAR:
            case LLVAR:
            case LLLVAR: {
                String length = StringUtils.padLeft(buffer.length, type.equals(DataElementType.LVAR) ? 1 : type.equals(DataElementType.LLVAR) ? 2 : 3, '0');

                MessageWriter mw = new MessageWriter();
                mw.addRange(length.getBytes());
                mw.addRange(buffer);

                return mw.toArray();
            }
            default: {
                return buffer;
            }
        }
    }

    private Iso8583Element() {}

    static Iso8583Element inflate(DataElementId id, DataElementType type, String description, Integer length, byte[] buffer) {
        Iso8583Element element = new Iso8583Element();
        element.id = id;
        element.type = type;
        element.description = description;
        element.length = length;
        element.buffer = buffer;

        return element;
    }
    static Iso8583Element inflate(DataElementId id, DataElementType type, String description, Integer length, MessageReader mr) {
        Iso8583Element element = new Iso8583Element();
        element.id = id;
        element.type = type;
        element.description = description;
        element.length = length;

        switch (type) {
            case LVAR:
            case LLVAR:
            case LLLVAR: {
                String lengthStr = mr.readString(type.equals(DataElementType.LVAR) ? 1 : type.equals(DataElementType.LLVAR) ? 2 : 3);
                Integer actualLength = Integer.parseInt(lengthStr);
                element.buffer = mr.readBytes(actualLength);
            }
            break;
            default: {
                element.buffer = mr.readBytes(length);
            }
        }

        return element;
    }

    <TResult extends IDataElement<TResult>> TResult getConcrete(Class<TResult> clazz) {
        try {
            TResult rvalue = clazz.newInstance();
            return rvalue.fromByteArray(buffer);
        }
        catch(Exception exc) {
            return null;
        }
    }
}
