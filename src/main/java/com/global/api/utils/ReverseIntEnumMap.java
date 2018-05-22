package com.global.api.utils;

import com.global.api.entities.enums.INumericConstant;

import java.util.HashMap;
import java.util.Map;

public class ReverseIntEnumMap<V extends Enum<V> & INumericConstant> {
    private Map<Integer, V> map = new HashMap<Integer, V>();

    public ReverseIntEnumMap(Class<V> valueType) {
        for(V v: valueType.getEnumConstants()) {
            map.put(v.getValue(), v);
        }
    }

    public V get(int value) {
        return map.get(value);
    }
}