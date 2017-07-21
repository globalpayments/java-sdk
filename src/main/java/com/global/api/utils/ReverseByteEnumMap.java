package com.global.api.utils;

import com.global.api.entities.enums.IByteConstant;

import java.util.HashMap;
import java.util.Map;

public class ReverseByteEnumMap<V extends Enum<V> & IByteConstant> {
    private Map<Byte, V> map = new HashMap();

    public ReverseByteEnumMap(Class<V> valueType) {
        for(V v: valueType.getEnumConstants()) {
            map.put(v.getByte(), v);
        }
    }

    public V get(byte value) {
        return map.get(value);
    }
}