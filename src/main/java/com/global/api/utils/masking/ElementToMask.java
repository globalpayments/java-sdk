package com.global.api.utils.masking;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"key", "value"})
public class ElementToMask {

    private final String key;
    private final String value;
    private final int unmaskedLastChars;
    private final int unmaskedFirstChars;
    private final char maskSymbol;

    public ElementToMask(String key, String value) {
        this(key, value, 0, 0, 'X');
    }

    public ElementToMask(String key, String value, int unmaskedLastChars, int unmaskedFirstChars) {
        this(key, value, unmaskedLastChars, unmaskedFirstChars, 'X');
    }

    public ElementToMask(String key, String value, int unmaskedLastChars, int unmaskedFirstChars, char maskSymbol) {
        this.key = key;
        this.value = value;
        this.unmaskedFirstChars = unmaskedFirstChars;
        this.unmaskedLastChars = unmaskedLastChars;
        this.maskSymbol = maskSymbol;
    }
}
