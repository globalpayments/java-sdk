package com.global.api.terminals.pax.subgroups;

import com.global.api.entities.enums.ControlCodes;
import com.global.api.entities.enums.PaxEntryMode;
import com.global.api.utils.EnumUtils;
import com.global.api.utils.MessageReader;
import com.global.api.utils.StringUtils;

public class AccountResponse {
    private String accountNumber;
    private PaxEntryMode entryMode;
    private String expireDate;
    private String ebtType;
    private String voucherNumber;
    private String newAccountNumber;
    private String cardType;
    private String cardHolder;
    private String cvdApprovalCode;
    private String cvdMessage;
    private boolean cardPresent;

    public String getAccountNumber() {
        return accountNumber;
    }
    public PaxEntryMode getEntryMode() {
        return entryMode;
    }
    public String getExpireDate() {
        return expireDate;
    }
    public String getEbtType() {
        return ebtType;
    }
    public String getVoucherNumber() {
        return voucherNumber;
    }
    public String getNewAccountNumber() {
        return newAccountNumber;
    }
    public String getCardType() {
        return cardType;
    }
    public String getCardHolder() {
        return cardHolder;
    }
    public String getCvdApprovalCode() {
        return cvdApprovalCode;
    }
    public String getCvdMessage() {
        return cvdMessage;
    }
    public boolean isCardPresent() {
        return cardPresent;
    }

    public AccountResponse(MessageReader br) {
        String values = br.readToCode(ControlCodes.FS);
        if (StringUtils.isNullOrEmpty(values))
            return;

        String[] data = values.split("\\[US\\]");
        try {
            accountNumber = data[0];
            entryMode = EnumUtils.parse(PaxEntryMode.class, data[1]);
            expireDate = data[2];
            ebtType = data[3];
            voucherNumber = data[4];
            newAccountNumber = data[5];
            cardType = data[6];
            cardHolder = data[7];
            cvdApprovalCode = data[8];
            cvdMessage = data[9];
            cardPresent = data[10].equals("0");
        }
        catch (IndexOutOfBoundsException e) {
            // Nom nom
        }
    }
}