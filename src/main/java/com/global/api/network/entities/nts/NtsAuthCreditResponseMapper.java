package com.global.api.network.entities.nts;

import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;

public class NtsAuthCreditResponseMapper implements INtsResponseMessage {
    private NtsCreditResponse ntsCreditResponse;
    public NtsCreditResponse getCreditMapper() {
        return ntsCreditResponse;
    }
    public void setCreditMapper(NtsCreditResponse ntsCreditResponse) {
        this.ntsCreditResponse = ntsCreditResponse;
    }

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvflag) {
        NtsAuthCreditResponseMapper ntsAuthCreditResponseMapper = new NtsAuthCreditResponseMapper();
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

        if (buffer.length > 28){
            if(emvflag) {
                ntsCreditResponse.setExpandedUserData(sp.readString(1)); // Emv
                NtsUtils.log("Expanded User Data", ntsCreditResponse.getExpandedUserData());

                ntsCreditResponse.setHostResponseLength(sp.readInt(4)); // Emv
                NtsUtils.log("Host Response Area Length", ntsCreditResponse.getHostResponseLength());

                ntsCreditResponse.setHostResponseArea(sp.readRemaining()); // Emv
                NtsUtils.log("Host Response Area", ntsCreditResponse.getHostResponseArea());

            } else {
                ntsCreditResponse.setHostResponseLength(sp.readInt(3));
                NtsUtils.log("Host Response Area Length", ntsCreditResponse.getHostResponseLength());

                ntsCreditResponse.setHostResponseArea(sp.readRemaining());
                NtsUtils.log("Host Response Area", ntsCreditResponse.getHostResponseArea());
            }
        }
        ntsAuthCreditResponseMapper.setCreditMapper(ntsCreditResponse);
        return ntsAuthCreditResponseMapper;
    }

}
