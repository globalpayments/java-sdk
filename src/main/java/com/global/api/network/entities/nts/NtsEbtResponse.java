package com.global.api.network.entities.nts;

import com.global.api.entities.enums.DebitAuthorizerCode;
import com.global.api.entities.enums.TransactionCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.StringParser;
import lombok.Getter;
import lombok.Setter;

public class NtsEbtResponse implements INtsResponseMessage {

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
    private String approvalCode;
    @Getter
    @Setter
    private DebitAuthorizerCode authorizerCode;
    @Getter
    @Setter
    private String terminalSequenceNumber;
    @Getter
    @Setter
    private int cashBenefit;
    @Getter
    @Setter
    private int foodStampBalance;
    @Getter
    @Setter
    private int cashBenefitLedgerBalance;
    @Getter
    @Setter
    private int cashBenefitBalance;
    @Getter
    @Setter
    private int foodStampLedgerBalance;

    @Override
    public INtsResponseMessage setNtsResponseMessage(byte[] buffer, boolean emvFlag) {
        NtsEbtResponse response = new NtsEbtResponse();
        StringParser sp = new StringParser(buffer);
        response.setCardType(sp.readStringConstant(2, NTSCardTypes.class));
        response.setTransactionCode(sp.readStringConstant(2, TransactionCode.class));
        response.setAccountType(sp.readString(3));
        response.setApprovalCode(sp.readString(6));
        response.setAuthorizerCode(sp.readStringConstant(2, DebitAuthorizerCode.class));
        response.setTerminalSequenceNumber(sp.readString(6));
        response.setCashBenefit(sp.readInt(7));
        response.setFoodStampBalance(sp.readInt(7));
        response.setCashBenefitLedgerBalance(sp.readInt(7));
        response.setFoodStampLedgerBalance(sp.readInt(7));
        return response;
    }
}
