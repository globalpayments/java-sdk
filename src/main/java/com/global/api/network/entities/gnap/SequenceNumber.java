package com.global.api.network.entities.gnap;

import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SequenceNumber {
    private int dayCounter;
    private int shiftCounter;
    private int batchCounter;
    private int sequenceCounter;
    private int indicator;

    public String getValue()
    {
        StringBuilder sb=new StringBuilder();
        sb.append(StringUtils.padLeft(dayCounter,3,'0'));
        sb.append(StringUtils.padLeft(shiftCounter,3,'0'));
        sb.append(StringUtils.padLeft(batchCounter,3,'0'));
        sb.append(StringUtils.padLeft(sequenceCounter,3,'0'));
        sb.append(StringUtils.padLeft(indicator,1,'0'));
        return  sb.toString();
    }
}
