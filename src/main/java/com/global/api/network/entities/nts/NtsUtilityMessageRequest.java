package com.global.api.network.entities.nts;

import com.global.api.builders.AuthorizationBuilder;
import com.global.api.builders.TransactionBuilder;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.entities.NtsObjectParam;
import com.global.api.paymentMethods.DebitTrackData;
import com.global.api.paymentMethods.GiftCard;
import com.global.api.paymentMethods.ICardData;
import com.global.api.utils.MessageWriter;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class NtsUtilityMessageRequest implements INtsRequestMessage {
    @Getter
    @Setter
    private Integer utilityType;
    @Getter
    @Setter
    private String fullVendorSoftwareVersion;
    @Getter
    @Setter
    private String reserved;
    @Getter
    @Setter
    ICardData iCardData;
    @Getter
    @Setter
    DebitTrackData debitTrackData;
    @Getter @Setter
    GiftCard giftCard;
    private static final String GROUP_SEPARATOR = "\u001D";

    @Override
    public MessageWriter setNtsRequestMessage(NtsObjectParam ntsObjectParam) throws BatchFullException {

        TransactionBuilder builder = ntsObjectParam.getNtsBuilder();
        MessageWriter request = ntsObjectParam.getNtsRequest();

        if (builder instanceof AuthorizationBuilder) {
            NtsUtilityMessageRequest ntsUtilityMessageRequest = ((AuthorizationBuilder) builder).getNtsUtilityMessageRequest();

            request.addRange(StringUtils.padLeft(ntsUtilityMessageRequest.getUtilityType().toString(), 3, '0'), 3);
            NtsUtils.log("Utility Type", StringUtils.padLeft(ntsUtilityMessageRequest.getUtilityType().toString(), 3, '0'));

            request.addRange(StringUtils.padLeft(ntsUtilityMessageRequest.getFullVendorSoftwareVersion(), 30, ' '), 30);
            NtsUtils.log("Full Vendor Software Version", ntsUtilityMessageRequest.getFullVendorSoftwareVersion());

            request.addRange(StringUtils.padLeft(ntsUtilityMessageRequest.getReserved(), 50, ' '), 50);
            NtsUtils.log("Reserved", ntsUtilityMessageRequest.getReserved());

            if(ntsObjectParam.getTokenData() != null){
                request.addRange(GROUP_SEPARATOR,1);
                request.addRange(ntsObjectParam.getTokenData(),216);
                NtsUtils.log("Token Tag DATA", ntsObjectParam.getTokenData());

            }
        }

        return request;
    }
}
