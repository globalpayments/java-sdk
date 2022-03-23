package com.global.api.utils;

import com.global.api.entities.enums.IByteConstant;
import com.global.api.entities.enums.IStringConstant;

public class StringParser {
    private int position = 0;
    private String buffer;

    public StringParser(byte[] buffer) {
        this(new String(buffer));
    }
    public StringParser(String str) {
        this.buffer = str;
    }

    public Boolean readBoolean() {
        return readBoolean("1");
    }
    public Boolean readBoolean(String indicator) {
        String value = readString(1);
        if(value == null) {
            return null;
        }
        return value.equals(indicator);
    }

    public <TResult extends Enum<TResult> & IByteConstant> TResult readByteConstant(Class<TResult> clazz) {
        String value = readString(1);
        if(value != null) {
            return ReverseByteEnumMap.parse(value.getBytes()[0], clazz);
        }
        return null;
    }

    public Byte readByte() {
        byte[] bytes = readBytes(1);
        if(bytes.length > 0) {
            return bytes[0];
        }
        return null;
    }
    public byte[] readBytes(int length) {
        String rvalue = readString(length);
        if(rvalue != null) {
            return rvalue.getBytes();
        }
        return new byte[0];
    }

    public Integer readInt(int length) {
        String value = readString(length);
        if(value != null) {
            return Integer.parseInt(value);
        }
        return null;
    }

    public String readLVAR() {
        return readVar(1);
    }
    public String readLLVAR() {
        return readVar(2);
    }
    public String readLLLVAR() {
        return readVar(3);
    }

    public String readRemaining() {
        if(position < buffer.length()) {
            String rvalue = buffer.substring(position);
            position = buffer.length();
            return rvalue;
        }
        return "";
    }
    public byte[] readRemainingBytes() {
        String rvalue = readRemaining();
        if(rvalue != null) {
            return rvalue.getBytes();
        }
        return new byte[0];
    }

    public String readString(int length) {
        int index = position + length;
        if(index > buffer.length()) {
            return null;
        }

        String rvalue = buffer.substring(position, index);
        position += length;
        return rvalue;
    }
    public <TResult extends Enum<TResult> & IStringConstant> TResult readStringConstant(int length, Class<TResult> clazz) {
        String value = readString(length);
        return ReverseStringEnumMap.parse(value, clazz);
    }

    public String readToChar(char c) {
        return readToChar(c, true);
    }
    public String readToChar(char c, boolean remove) {
        int index = buffer.indexOf(c, position);
        if(index < 0) {
            return readRemaining();
        }

        String rvalue = buffer.substring(position, index);
        position = index;
        if(remove) {
            position++;
        }

        return StringUtils.isNullOrEmpty(rvalue) ? "" : rvalue;
    }

    private String readVar(int length) {
        Integer actual = readInt(length);
        if(actual != null) {
            return readString(actual);
        }
        return null;
    }

    public String getBuffer() {
        return buffer;
    }

    public Integer getRemainingLength(){
        return buffer.length() - position;
    }
}
