package com.global.api.network.entities.nts;

import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsVoidReversalResponse implements INtsResponseMessage {

    private NtsCreditResponse ntsCreditResponse;
    public NtsCreditResponse getCreditMapper() {
        return ntsCreditResponse;
    }
    public void setCreditMapper(NtsCreditResponse ntsCreditResponse) {
        this.ntsCreditResponse = ntsCreditResponse;
    }

    @Setter
    @Getter
    private String authorizationCode;

    @Setter
    @Getter
    private String originalTransactionDate;

    @Setter
    @Getter
    private String originalTransactionTime;

    @Setter
    @Getter
    private	Integer batchNumber;

    @Setter
    @Getter
    private	Integer sequenceNumber;
    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsVoidReversalResponse ntsSaleCreditResponseMapper = new NtsVoidReversalResponse();
        ntsCreditResponse = new NtsCreditResponse();
        StringParser sp = new StringParser(buffer);

        ntsSaleCreditResponseMapper.setAuthorizationCode(sp.readString(2));
        ntsSaleCreditResponseMapper.setOriginalTransactionDate(sp.readString(4));
        ntsSaleCreditResponseMapper.setOriginalTransactionTime(sp.readString(6));
        ntsCreditResponse.setCardType(sp.readStringConstant(2, NTSCardTypes.class));
        ntsCreditResponse.setAccountNumber(sp.readString(19));
        ntsCreditResponse.setApprovalCode(sp.readString(6));
        ntsCreditResponse.setAuthorizer(sp.readStringConstant(1, AuthorizerCode.class));
        ntsSaleCreditResponseMapper.setBatchNumber(sp.readInt(2));
        ntsSaleCreditResponseMapper.setSequenceNumber(sp.readInt(3));

        // When the host response area is included in the response
        // Then only the length of message is more than 85.
        if(buffer.length > 85) {
            if (emvFlag) {
                ntsCreditResponse.setExpandedUserData(sp.readString(1));
                ntsCreditResponse.setHostResponseLength(sp.readInt(4)); // Emv
                ntsCreditResponse.setHostResponseArea(sp.readRemaining()); // Emv
            } else {
                ntsCreditResponse.setHostResponseLength(sp.readInt(3));
                ntsCreditResponse.setHostResponseArea(sp.readRemaining());
            }
        }
        ntsSaleCreditResponseMapper.setCreditMapper(ntsCreditResponse);
        return ntsSaleCreditResponseMapper;
    }
}
