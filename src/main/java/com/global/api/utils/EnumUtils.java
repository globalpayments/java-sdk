package com.global.api.utils;

import com.global.api.entities.enums.IByteConstant;
import com.global.api.entities.enums.INumericConstant;
import com.global.api.entities.enums.IStringConstant;

public class EnumUtils {
    public static <V extends Enum<V> & IByteConstant> boolean isDefined(Class<V> valueType, byte value){
        return parse(valueType, value) != null;
    }

    public static <V extends Enum<V> & IByteConstant> V parse(Class<V> valueType, byte value) {
        ReverseByteEnumMap<V> map = new ReverseByteEnumMap<V>(valueType);
        return map.get(value);
    }

    public static <V extends Enum<V> & IStringConstant> V parse(Class<V> valueType, String value) {
        ReverseStringEnumMap<V> map = new ReverseStringEnumMap<V>(valueType);
        return map.get(value);
    }

    public static <V extends Enum<V> & INumericConstant> V parse(Class<V> valueType, int value) {
        ReverseIntEnumMap<V> map = new ReverseIntEnumMap<V>(valueType);
        return map.get(value);
    }
}
