package com.global.api.network.entities.nts;

import com.global.api.network.enums.AuthorizerCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.NtsUtils;
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
        NtsUtils.log("Authorizer Code", ntsSaleCreditResponseMapper.getAuthorizationCode());

        ntsSaleCreditResponseMapper.setOriginalTransactionDate(sp.readString(4));
        NtsUtils.log("Original Transaction Date", ntsSaleCreditResponseMapper.getOriginalTransactionDate());

        ntsSaleCreditResponseMapper.setOriginalTransactionTime(sp.readString(6));
        NtsUtils.log("Original Transaction Time", ntsSaleCreditResponseMapper.getOriginalTransactionTime());

        ntsCreditResponse.setCardType(sp.readStringConstant(2, NTSCardTypes.class));
        NtsUtils.log("Card Type", ntsCreditResponse.getCardType());


        ntsCreditResponse.setAccountNumber(sp.readString(19));
        NtsUtils.log("Account Number", ntsCreditResponse.getAccountNumber());

        ntsCreditResponse.setApprovalCode(sp.readString(6));
        NtsUtils.log("Approval Code", ntsCreditResponse.getApprovalCode());

        ntsSaleCreditResponseMapper.setBatchNumber(sp.readInt(2));
        NtsUtils.log("Batch Number", ntsSaleCreditResponseMapper.getBatchNumber());

        ntsSaleCreditResponseMapper.setSequenceNumber(sp.readInt(3));
        NtsUtils.log("Sequence Number", ntsSaleCreditResponseMapper.getSequenceNumber());


        // When the host response area is included in the response
        // Then only the length of message is more than 85.
        if(buffer.length > 85) {
            if (emvFlag) {
                ntsCreditResponse.setExpandedUserData(sp.readString(1));
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
        ntsSaleCreditResponseMapper.setCreditMapper(ntsCreditResponse);
        return ntsSaleCreditResponseMapper;
    }
}
