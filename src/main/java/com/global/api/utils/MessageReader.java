package com.global.api.utils;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.IByteConstant;

public class MessageReader {
    byte[] buffer;
    int position = 0;
    long length = 0;

    public long getLength() { return length; }

    public MessageReader(byte[] bytes){
        buffer = bytes;
        length = bytes.length;
    }

    public boolean canRead(){
        return position < length;
    }

    public byte peek() {
        return buffer[position];
    }

    public ControlCodes readCode(){
        return readEnum(ControlCodes.class);
        //return ControlCode.convert(buffer[position++]);
    }

    public <T extends Enum<T> & IByteConstant> T readEnum(Class<T> enumType){
        ReverseByteEnumMap<T> map = new ReverseByteEnumMap<T>(enumType);
        return map.get(buffer[position++]);
    }

    public byte readByte() {
        return buffer[position++];
    }

    public byte[] readBytes(int length){
        byte[] rvalue = new byte[length];

        try {
            for (int i = 0; i < length; i++)
                rvalue[i] = buffer[position++];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            // eat this exception and return what we have
        }

        return rvalue;
    }

    public char readChar(){
        return (char)buffer[position++];
    }

    public String readString(int length){
        String rvalue = "";

        for(int i = 0; i < length; i++)
            rvalue += (char)buffer[position++];

        return rvalue;
    }

    public String readToCode(ControlCodes code) {
        return readToCode(code, true);
    }
    public String readToCode(ControlCodes code, boolean removeCode){
        String rvalue = "";

        try {
            byte value;
            while((value = peek()) != code.getByte()) {
                if(EnumUtils.isDefined(ControlCodes.class, value)) {
                    ControlCodes byteCode = EnumUtils.parse(ControlCodes.class, buffer[position++]);
                    if(byteCode == ControlCodes.ETX)
                        break;
                    else rvalue += byteCode.toString();
                } else rvalue += (char)buffer[position++];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            removeCode = false;
        }

        // pop the code off
        if(removeCode)
            readByte();

        return rvalue;
    }

    public void purge(){
        buffer = new byte[0];
        length = 0;
    }
}