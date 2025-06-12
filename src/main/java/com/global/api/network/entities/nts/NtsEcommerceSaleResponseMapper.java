package com.global.api.network.entities.nts;

import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

public class NtsEcommerceSaleResponseMapper implements INtsResponseMessage {

    private NtsCreditResponse ntsCreditResponse;
    @Getter
    @Setter
    private int batchNumber;
    @Getter
    @Setter
    private int sequenceNumber;

    public NtsCreditResponse getCreditMapper() {
        return ntsCreditResponse;
    }

    public void setCreditMapper(NtsCreditResponse ntsCreditResponse) {
        this.ntsCreditResponse = ntsCreditResponse;
    }

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsEcommerceSaleResponseMapper ntsEcommerceSaleResponseMapper = new NtsEcommerceSaleResponseMapper();
        ntsCreditResponse = new NtsCreditResponse();
        StringParser sp = new StringParser(buffer);

        ntsCreditResponse.setCardType(sp.readStringConstant(2, NTSCardTypes.class));
        NtsUtils.log("Card Type", ntsCreditResponse.getCardType());

        ntsCreditResponse.setAccountNumber(sp.readString(19));
        NtsUtils.log("Account Number", StringUtils.maskAccountNumber(ntsCreditResponse.getAccountNumber()));

        ntsCreditResponse.setApprovalCode(sp.readString(6));
        NtsUtils.log("Approval Code", ntsCreditResponse.getApprovalCode());

        ntsCreditResponse.setAuthorizer(sp.readStringConstant(1, AuthorizerCode.class));
        NtsUtils.log("Authorizer", ntsCreditResponse.getAuthorizer());

        ntsEcommerceSaleResponseMapper.setBatchNumber(sp.readInt(2));
        NtsUtils.log("Batch Number", ntsEcommerceSaleResponseMapper.getBatchNumber());

        ntsEcommerceSaleResponseMapper.setSequenceNumber(sp.readInt(3));
        NtsUtils.log("Sequence Number", ntsEcommerceSaleResponseMapper.getSequenceNumber());

        if (buffer.length > 33) {
            if(!ntsCreditResponse.getCardType().equals(NTSCardTypes.WexFleet)) {
                ntsCreditResponse.setExpandedUserData(sp.readString(1));
                NtsUtils.log("Expanded User Data", ntsCreditResponse.getExpandedUserData());

                ntsCreditResponse.setHostResponseLength(sp.readInt(4));
                NtsUtils.log("Host Response Area Length", ntsCreditResponse.getHostResponseLength());
            }
            else {
                ntsCreditResponse.setHostResponseLength(sp.readInt(3));
                NtsUtils.log("Host Response Area Length", ntsCreditResponse.getHostResponseLength());
            }
            ntsCreditResponse.setHostResponseArea(sp.readRemaining());
            NtsUtils.log("Host Response Area", ntsCreditResponse.getHostResponseArea());
        }
        ntsEcommerceSaleResponseMapper.setCreditMapper(ntsCreditResponse);
        return ntsEcommerceSaleResponseMapper;
    }
}
