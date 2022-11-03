package com.global.api.network.entities.nts;

import com.global.api.entities.enums.DebitAuthorizerCode;
import com.global.api.entities.enums.TransactionCode;
import com.global.api.network.enums.NTSCardTypes;
import com.global.api.utils.NtsUtils;
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
        NtsUtils.log("Card Type", response.getCardType());

        response.setTransactionCode(sp.readStringConstant(2, TransactionCode.class));
        NtsUtils.log("Transaction Code", response.getTransactionCode());

        response.setAccountType(sp.readString(3));
        NtsUtils.log("Account Type", response.getAccountType());

        response.setApprovalCode(sp.readString(6));
        NtsUtils.log("Approval Code", response.getApprovalCode());

        response.setAuthorizerCode(sp.readStringConstant(2, DebitAuthorizerCode.class));
        NtsUtils.log("Debit Authorizer Code", response.getAuthorizerCode());

        response.setTerminalSequenceNumber(sp.readString(6));
        NtsUtils.log("Terminal Sequence Number", response.getTerminalSequenceNumber());

        response.setCashBenefitBalance(sp.readInt(7));
        NtsUtils.log("REMAINING Cash Benefit balance", response.getCashBenefitBalance());

        response.setFoodStampBalance(sp.readInt(7));
        NtsUtils.log("REMAINING Food Stamp balance", response.getFoodStampBalance());

        response.setCashBenefitLedgerBalance(sp.readInt(7));
        NtsUtils.log("Cash Benefit Ledger balance", response.getCashBenefitLedgerBalance());

        response.setFoodStampLedgerBalance(sp.readInt(7));
        NtsUtils.log("Food Stamp Ledger balance", response.getFoodStampLedgerBalance());

        return response;
    }
}
