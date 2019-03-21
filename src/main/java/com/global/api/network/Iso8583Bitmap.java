package com.global.api.network;

import com.global.api.network.enums.DataElementId;
import com.global.api.utils.ReverseIntEnumMap;
import com.global.api.utils.StringUtils;

import java.math.BigInteger;

public class Iso8583Bitmap {
    private String binaryValue;
    private int offset;
    private int currIndex = -1;

    private ReverseIntEnumMap<DataElementId> dataElementMap;

    public Iso8583Bitmap(byte[] bytes) {
        this(bytes, 0);
    }
    public Iso8583Bitmap(byte[] bytes, int offset) {
        this.offset = offset;

        StringBuilder sb = new StringBuilder();
        for(byte b: bytes) {
            sb.append(Integer.toBinaryString((b & 0xFF) + 0x100).substring(1));
        }

        binaryValue = sb.toString();
        dataElementMap = new ReverseIntEnumMap<DataElementId>(DataElementId.class);
    }

    public boolean isPresent(DataElementId element) {
        return binaryValue.charAt(element.getValue() - offset) == '1';
    }

    public DataElementId getNextDataElement() {
        // get the next set value
        char value;
        do {
            // return null if end of string
            if(++currIndex >= binaryValue.length()) {
                return null;
            }

            value = binaryValue.charAt(currIndex);
        }
        while (value == '0');

        // return the enum value
        return dataElementMap.get(currIndex + offset);
    }

    void setDataElement(DataElementId element) {
        StringBuilder sb = new StringBuilder(binaryValue);
        sb.setCharAt(element.getValue() - offset, '1');
        binaryValue = sb.toString();
    }

    public String toBinaryString() {
        return binaryValue;
    }
    public String toHexString() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < binaryValue.length(); i += 8) {
            int decimal = Integer.parseInt(binaryValue.substring(i, i + 8), 2);
            String hexValue = Integer.toString(decimal, 16);
            sb.append(StringUtils.padLeft(hexValue, 2, '0'));
        }

        return sb.toString();
    }
    public byte[] toByteArray() {
        String s = toHexString();

        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }
}
