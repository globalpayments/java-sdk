package com.global.api.utils;

import com.global.api.entities.enums.IByteConstant;
import com.global.api.entities.enums.IStringConstant;
import java.util.ArrayList;
import java.util.List;

public class MessageWriter {
    List<Byte> buffer;
    StringBuilder messageRequest;
    static final char ZERO_CHAR = '0';
    public StringBuilder getMessageRequest() {
        return messageRequest;
    }

    public void setMessageRequest(StringBuilder messageRequest) {
        this.messageRequest = messageRequest;
    }

    public MessageWriter() {
        buffer = new ArrayList<>();
        messageRequest=new StringBuilder();
    }

    public MessageWriter(byte[] bytes){
        buffer = new ArrayList<>();
        for(byte b: bytes)
            buffer.add(b);
    }

    public void add(Byte b) { buffer.add(b); }

    public void add(IByteConstant constant){
        buffer.add(constant.getByte());
    }

    public void add(IStringConstant constant) {
        for(byte b: constant.getBytes())
            buffer.add(b);
    }

    public void addRange(Byte[] bytes) {
        for(byte b: bytes)
            buffer.add(b);
    }

    public void addRange(byte[] bytes){
        for(byte b: bytes)
            buffer.add(b);
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

    public void add(Integer value) {
        byte[] bytes = formatInteger(value.longValue(), 1);
        addRange(bytes);
    }

    public void add(Integer value, Integer length) {
        byte[] bytes = formatInteger(value.longValue(), length);
        addRange(bytes);
    }

    public void add(String value) {
        addRange(value.getBytes());
    }

    private static byte[] intToBytes(int data) {
        return new byte[] {
                (byte)((data >> 24) & 0xff),
                (byte)((data >> 16) & 0xff),
                (byte)((data >> 8) & 0xff),
                (byte)((data >> 0) & 0xff),
        };
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

            MessageWriter inputBuffer = new MessageWriter();
            for (int i = 0; i < baseLength; i++) {
                int offset = offsets[baseLength - 1 - i];
                inputBuffer.add((byte) (value >>> offset));
            }
            byte[] input = inputBuffer.toArray();

            byte[] output = new byte[length];
            System.arraycopy(input, 0, output, length - baseLength, baseLength);

            return output;
        }
    }
    public StringBuilder addRange(String fieldValue,Integer digitCount)
    {
        if(fieldValue != null && digitCount != null) {
            if(fieldValue.length() == digitCount) {
                return messageRequest.append(fieldValue);
            } else if(fieldValue.length() > digitCount){
                return  messageRequest.append(fieldValue, 0, digitCount);
            } else {
                fieldValue = StringUtils.padLeft(fieldValue, digitCount, ZERO_CHAR);
                return messageRequest.append(fieldValue);
            }
        }
        else
            return messageRequest;
    }
    public StringBuilder addRange(Integer fieldValue, Integer digitCount){
        return addRange(String.valueOf(fieldValue), digitCount);
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
