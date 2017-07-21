package com.global.api.utils;

import com.global.api.entities.enums.IByteConstant;
import com.global.api.entities.enums.IStringConstant;

import java.util.ArrayList;
import java.util.List;

public class MessageWriter {
    List<Byte> buffer;

    public MessageWriter() {
        buffer = new ArrayList<Byte>();
    }

    public MessageWriter(byte[] bytes){
        buffer = new ArrayList<Byte>();
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
}
