package com.global.api.utils;

import com.global.api.entities.enums.*;

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

    public static String getMapping(Target target, IMappedConstant value) {
        return (value != null) ? value.getValue(target) : null;
    }

    public static String mapDigitalWalletType(Target target, MobilePaymentMethodType type) {
        if (target == Target.GP_API) {
            switch (type) {
                case APPLEPAY:
                    return EncyptedMobileType.APPLE_PAY.getValue();
                case GOOGLEPAY:
                    return EncyptedMobileType.GOOGLE_PAY.getValue();
                case CLICK_TO_PAY:
                    return EncyptedMobileType.CLICK_TO_PAY.getValue();
            }
        }
        return null;
    }

}
