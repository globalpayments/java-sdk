package com.global.api.network.entities.nts;

import com.global.api.entities.enums.PendingMailIndicator;
import com.global.api.entities.enums.PendingParameterIndicator;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NtsRequestPendingMessagesResponse implements INtsResponseMessage {

    private PendingMailIndicator pendingMailIndicator;
    private PendingParameterIndicator pendingParameterIndicator;
    private String pendingFutureIndicators;

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsRequestPendingMessagesResponse ntsResponse = new NtsRequestPendingMessagesResponse();
        StringParser sp = new StringParser(buffer);

        ntsResponse.setPendingMailIndicator(sp.readStringConstant(1, PendingMailIndicator.class));
        ntsResponse.setPendingParameterIndicator(sp.readStringConstant(1,PendingParameterIndicator.class));
        ntsResponse.setPendingFutureIndicators(sp.readRemaining());
        return ntsResponse;
    }
}



