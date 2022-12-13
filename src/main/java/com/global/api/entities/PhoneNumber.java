package com.global.api.entities;

import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
public class PhoneNumber {
    private String countryCode;
    private String areaCode;
    private String number;
    private String extension;

    public String toString() {
        StringBuilder sb = new StringBuilder();

        // country code (default to 1)
        if(StringUtils.isNullOrEmpty(countryCode)) {
            countryCode = "1";
        }
        sb.append("+".concat(countryCode));

        // append area code if present
        if(!StringUtils.isNullOrEmpty(areaCode)) {
            sb.append(String.format("(%s)", areaCode));
        }

        // put the number
        sb.append(number);

        // put extension if present
        if(!StringUtils.isNullOrEmpty(extension)) {
            sb.append(String.format("EXT: %s", extension));
        }

        return sb.toString();
    }
}
