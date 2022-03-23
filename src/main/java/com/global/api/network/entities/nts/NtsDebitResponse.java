package com.global.api.network.entities.nts;

import com.global.api.entities.enums.DebitAuthorizerCode;
import com.global.api.entities.enums.TransactionCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsDebitResponse implements INtsResponseMessage {
    @Getter
    @Setter
    private NTSCardTypes cardType;
    @Getter
    @Setter
    private TransactionCode transactionCode;
    @Getter
    @Setter
    private String accountType;
    @Getter
    @Setter
    private String code;
    @Getter
    @Setter
    private DebitAuthorizerCode authorizerCode;
    @Getter
    @Setter
    private String terminalSequenceNumber;
    @Getter
    @Setter
    private int amount;
    @Getter
    @Setter
    private int emvDataLength;
    @Getter
    @Setter
    private String emvData;

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer , boolean emvFlag) {
        NtsDebitResponse ntsDebitResponse = new NtsDebitResponse();
        StringParser sp = new StringParser(buffer);

        ntsDebitResponse.setCardType(sp.readStringConstant(2, NTSCardTypes.class));
        ntsDebitResponse.setTransactionCode(sp.readStringConstant(2, TransactionCode.class));
        ntsDebitResponse.setAccountType(sp.readString(3));
        ntsDebitResponse.setCode(sp.readString(6));
        ntsDebitResponse.setAuthorizerCode(sp.readStringConstant(2, DebitAuthorizerCode.class));
        ntsDebitResponse.setTerminalSequenceNumber(sp.readString(6));
        String sAvailableAmount = sp.readString(7).trim();
        Integer iAvailableAmount = sAvailableAmount.length() > 0 ? Integer.parseInt(sAvailableAmount):Integer.parseInt("0");
        ntsDebitResponse.setAmount(iAvailableAmount);
        if(buffer.length > 89 && emvFlag) {
            ntsDebitResponse.setEmvDataLength(sp.readInt(4)); // Emv
            ntsDebitResponse.setEmvData(sp.readRemaining()); // Emv
        }
        return ntsDebitResponse;
    }
}
