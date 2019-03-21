package com.global.api.utils;

import com.global.api.entities.enums.IStringConstant;

import java.util.HashMap;
import java.util.Map;

public class ReverseStringEnumMap<V extends Enum<V> & IStringConstant> {
    private Map<String, V> map = new HashMap<String, V>();

    public ReverseStringEnumMap(Class<V> valueType) {
        for(V v: valueType.getEnumConstants()) {
            map.put(v.getValue(), v);
        }
    }

    public V get(String value) {
        return map.get(value);
    }

    public static <TResult extends Enum<TResult> & IStringConstant> TResult parse(String value, Class<TResult> clazz) {
        ReverseStringEnumMap<TResult> mapper = new ReverseStringEnumMap<TResult>(clazz);
        return mapper.get(value);
    }
}