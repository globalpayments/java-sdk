package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;

public class NtsMailRequest extends NtsNetworkMessageHeader implements INtsRequestMessage {

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) {
        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();

        //message body
        AuthorizationBuilder authorizationBuilder;

        if (builder instanceof AuthorizationBuilder) {
            authorizationBuilder = (AuthorizationBuilder) builder;

            NtsMailData ntsMailData = authorizationBuilder.getNtsMailData();
            NtsUtils.log("MAIL COUNT", ntsMailData.getMailCount().toString());
            request.addRange(ntsMailData.getMailCount(), 2);

            NtsUtils.log("MAIL INDICATOR", ntsMailData.getMailIndicator().getValue());
            request.addRange(ntsMailData.getMailIndicator().getValue(), 1);

            NtsUtils.log("MAIL MESSAGE Type", ntsMailData.getMailMessageType().toString());
            request.addRange(ntsMailData.getMailMessageType(), 1);

            NtsUtils.log("MAIL MESSAGE Code", "8");
            request.addRange(ntsMailData.getMailMessageCode().getValue(), 1);

            if (ntsMailData.getMailText() != null && ntsMailData.getMailText().length() > 0) {
                Integer msgLength = ntsMailData.getMailText().length();

                NtsUtils.log("MAIL TEXT LENGTH", msgLength.toString());
                request.addRange(ntsMailData.getMailText().length(), 3);

                NtsUtils.log("MAIL TEXT ", ntsMailData.getMailText());
                request.addRange(ntsMailData.getMailText(), ntsMailData.getMailText().length());
            }
        }
        return request;
    }
}
