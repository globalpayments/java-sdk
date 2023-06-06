package com.global.api.network.entities.nts;

import com.global.api.entities.enums.DebitAuthorizerCode;
import com.global.api.entities.enums.TransactionCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.NtsUtils;
import com.global.api.utils.StringParser;
import com.global.api.utils.StringUtils;
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
        NtsUtils.log("Card Type", ntsDebitResponse.getCardType());

        ntsDebitResponse.setTransactionCode(sp.readStringConstant(2, TransactionCode.class));
        NtsUtils.log("Transaction Code", ntsDebitResponse.getTransactionCode());

        ntsDebitResponse.setAccountType(sp.readString(3));
        NtsUtils.log("Account Type", ntsDebitResponse.getAccountType());

        ntsDebitResponse.setCode(sp.readString(6));
        NtsUtils.log("Approval Code", ntsDebitResponse.getCode());

        ntsDebitResponse.setAuthorizerCode(sp.readStringConstant(2, DebitAuthorizerCode.class));
        NtsUtils.log("Debit Authorizer", ntsDebitResponse.getAuthorizerCode());

        ntsDebitResponse.setTerminalSequenceNumber(sp.readString(6));
        NtsUtils.log("Terminal Sequence Number", ntsDebitResponse.getTerminalSequenceNumber());

        String sAvailableAmount = sp.readString(7).trim();
        Integer iAvailableAmount = sAvailableAmount.length() > 0 ? Integer.parseInt(sAvailableAmount):Integer.parseInt("0");
        ntsDebitResponse.setAmount(iAvailableAmount);
        String amount= StringUtils.padLeft(iAvailableAmount.toString(),7, '0');
        NtsUtils.log("Available Amount", amount);

        if(buffer.length > 28 && emvFlag) {
            ntsDebitResponse.setEmvDataLength(sp.readInt(4)); // Emv
            NtsUtils.log("EMV Data Length", ntsDebitResponse.getEmvDataLength());

            ntsDebitResponse.setEmvData(sp.readRemaining()); // Emv
            NtsUtils.log("EMV Data", ntsDebitResponse.getEmvData());

        }
        return ntsDebitResponse;
    }
}
