package com.global.api.network.entities.nts;

import com.global.api.entities.enums.*;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsMailResponse implements INtsResponseMessage {
    @Getter
    @Setter
    private int mailCount;
    @Getter
    @Setter
    private MailIndicatorType mailIndicator;
    @Getter
    @Setter
    private MailMessageCodeType mailMessageCode;
    @Getter
    @Setter
    private String mailText;

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsMailResponse ntsResponse = new NtsMailResponse();
        StringParser sp = new StringParser(buffer);

        ntsResponse.setMailCount(sp.readInt(2));
        ntsResponse.setMailIndicator(sp.readStringConstant(1, MailIndicatorType.class));
        ntsResponse.setMailMessageCode(sp.readStringConstant(1, MailMessageCodeType.class));
        ntsResponse.setMailText(sp.readRemaining());

        return ntsResponse;
    }
}
