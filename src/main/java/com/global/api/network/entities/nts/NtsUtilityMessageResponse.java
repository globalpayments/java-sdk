package com.global.api.network.entities.nts;

import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsUtilityMessageResponse implements INtsResponseMessage {
    @Getter
    @Setter
    private Integer utilityType;
    @Getter
    @Setter
    private String utcDate;
    @Getter
    @Setter
    private String utcTime;
    @Getter
    @Setter
    private String reserved;

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsUtilityMessageResponse ntsUtilityMessageResponse = new NtsUtilityMessageResponse();
        StringParser sp = new StringParser(buffer);
        ntsUtilityMessageResponse.setUtilityType(sp.readInt(3));
        NtsUtils.log("Utility Type", ntsUtilityMessageResponse.getUtilityType());

        ntsUtilityMessageResponse.setUtcDate(sp.readString(8));
        NtsUtils.log("UTC Date", ntsUtilityMessageResponse.getUtcDate());

        ntsUtilityMessageResponse.setUtcTime(sp.readString(6));
        NtsUtils.log("UTC Time", ntsUtilityMessageResponse.getUtcTime());

        ntsUtilityMessageResponse.setReserved(sp.readRemaining());
        NtsUtils.log("Reserved", ntsUtilityMessageResponse.getReserved());

        return ntsUtilityMessageResponse;
    }
}
