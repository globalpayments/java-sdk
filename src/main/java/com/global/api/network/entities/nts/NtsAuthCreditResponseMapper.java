package com.global.api.network.entities.nts;

import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.StringParser;

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
        ntsCreditResponse.setAccountNumber(sp.readString(19));
        ntsCreditResponse.setApprovalCode(sp.readString(6));
        ntsCreditResponse.setAuthorizer(sp.readStringConstant(1, AuthorizerCode.class));

        if (buffer.length > 28){
            if(emvflag) {
                ntsCreditResponse.setExpandedUserData(sp.readString(1)); // Emv
                ntsCreditResponse.setHostResponseLength(sp.readInt(4)); // Emv
                ntsCreditResponse.setHostResponseArea(sp.readRemaining()); // Emv
            } else {
                ntsCreditResponse.setHostResponseLength(sp.readInt(3));
                ntsCreditResponse.setHostResponseArea(sp.readRemaining());
            }
        }
        ntsAuthCreditResponseMapper.setCreditMapper(ntsCreditResponse);
        return ntsAuthCreditResponseMapper;
    }

}
