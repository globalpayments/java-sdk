package com.global.api.utils;

import com.global.api.entities.enums.IByteConstant;
import com.global.api.entities.enums.IStringConstant;

import java.util.ArrayList;
import java.util.List;

public class NetworkMessageBuilder {
    private List<Byte> buffer;

    public NetworkMessageBuilder() {
        buffer = new ArrayList<Byte>();
    }

    public NetworkMessageBuilder(byte[] bytes){
        buffer = new ArrayList<Byte>();
        for(byte b: bytes)
            buffer.add(b);
    }

    public NetworkMessageBuilder append(Byte b) {
        buffer.add(b);
        return this;
    }

    public NetworkMessageBuilder append(IByteConstant constant){
        return append(constant, 1);
    }
    public NetworkMessageBuilder append(IByteConstant constant, int length){
        return append((int)constant.getByte());
//        byte[] bytes = formatInteger((int)constant.getByte(), length);
//        return append(bytes);
    }

    public NetworkMessageBuilder append(Integer value) {
        return append(value, 1);
    }
    public NetworkMessageBuilder append(Integer value, int length) {
        byte[] bytes = formatInteger(value.longValue(), length);
        return append(bytes);
    }

    public NetworkMessageBuilder append(Long value) {
        return append(value, 1);
    }
    public NetworkMessageBuilder append(Long value, int length) {
        byte[] bytes = formatInteger(value, length);
        return append(bytes);
    }

    public NetworkMessageBuilder append(IStringConstant constant) {
        return append(constant.getBytes());
    }

    public NetworkMessageBuilder append(String value) {
        return append(value.getBytes());
    }

    public NetworkMessageBuilder append(Byte[] bytes) {
        for(byte b: bytes)
            buffer.add(b);

        return this;
    }

    public NetworkMessageBuilder append(byte[] bytes){
        for(byte b: bytes)
            buffer.add(b);

        return this;
    }

    public void pop(){
        buffer.remove(buffer.size() - 1);
    }

    public byte[] toArray(){
        byte[] b = new byte[buffer.size()];

        Object[] b2 = buffer.toArray();
        for(int i = 0; i < buffer.size(); i++)
            b[i] = (Byte)b2[i];

        return b;
    }

    private byte[] formatInteger(Long value, Integer length) {
        int[] offsets = { 0, 8, 16, 32, 64, 128, 256, 512, 1024, 2048 };

        if(length == 1) {
            return new byte[] { (byte)(value & 0xFF) };
        }
        else {
            int byteCount = Math.abs(Long.bitCount(value) / 8) + 1;
            int baseLength = byteCount * 2;
            if(baseLength > length) { baseLength = length; }

            MessageWriter buffer = new MessageWriter();
            for (int i = 0; i < baseLength; i++) {
                int offset = offsets[baseLength - 1 - i];
                buffer.add((byte) (value >>> offset));
            }
            byte[] input = buffer.toArray();

            byte[] output = new byte[length];
            System.arraycopy(input, 0, output, length - baseLength, baseLength);

            return output;
        }
    }

    public String toString() {
        char[] HEX_CHARS = "0123456789abcdef".toCharArray();
        char[] chars = new char[2 * buffer.size()];
        for (int i = 0; i < buffer.size(); ++i) {
            chars[2 * i] = HEX_CHARS[(buffer.get(i) & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buffer.get(i) & 0x0F];
        }
        return new String(chars);
    }

    public int length() {
        return buffer.size();
    }
}
