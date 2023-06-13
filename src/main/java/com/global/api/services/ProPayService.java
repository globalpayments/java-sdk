package com.global.api.services;

import com.global.api.builders.ProPayBuilder;
import com.global.api.entities.enums.TransactionModifier;
import com.global.api.entities.enums.TransactionType;

public class ProPayService {
    public ProPayBuilder createAccount() {
        return new ProPayBuilder(TransactionType.CreateAccount);
    }

    public ProPayBuilder editAccount() {
        return new ProPayBuilder(TransactionType.EditAccount);
    }

    public ProPayBuilder resetPassword() {
        return new ProPayBuilder(TransactionType.ResetPassword);
    }

    public ProPayBuilder renewAccount() {
        return new ProPayBuilder(TransactionType.RenewAccount);
    }

    public ProPayBuilder updateBeneficialOwnershipInfo() {
        return new ProPayBuilder(TransactionType.UpdateBeneficialOwnership);
    }

    public ProPayBuilder disownAccount() {
        return new ProPayBuilder(TransactionType.DisownAccount);
    }

    public ProPayBuilder uploadDocumentChargeback() {
        return new ProPayBuilder(TransactionType.UploadDocumentChargeback);
    }

    public ProPayBuilder uploadDocument() {
        return new ProPayBuilder(TransactionType.UploadDocument);
    }

    public ProPayBuilder obtainSSOKey() {
        return new ProPayBuilder(TransactionType.ObtainSSOKey);
    }

    public ProPayBuilder updateBankAccountOwnershipInfo() {
        return new ProPayBuilder(TransactionType.UpdateBankAccountOwnership);
    }

    public ProPayBuilder addFunds() {
        return new ProPayBuilder(TransactionType.AddFunds);
    }

    public ProPayBuilder sweepFunds() {
        return new ProPayBuilder(TransactionType.SweepFunds);
    }

    public ProPayBuilder addCardFlashFunds() {
        return new ProPayBuilder(TransactionType.AddCardFlashFunds);
    }

    public ProPayBuilder pushMoneyToFlashFundsCard() {
        return new ProPayBuilder(TransactionType.PushMoneyFlashFunds);
    }

    public ProPayBuilder disburseFunds() {
        return new ProPayBuilder(TransactionType.DisburseFunds);
    }

    public ProPayBuilder spendBack() {
        return new ProPayBuilder(TransactionType.SpendBack);
    }

    public ProPayBuilder reverseSplitPay() {
        return new ProPayBuilder(TransactionType.ReverseSplitPay);
    }

    public ProPayBuilder splitFunds() {
        return new ProPayBuilder(TransactionType.SplitFunds);
    }

    public ProPayBuilder getAccountDetails() {
        return new ProPayBuilder(TransactionType.GetAccountDetails);
    }

    public ProPayBuilder getAccountDetailsEnhanced() {
        return new ProPayBuilder(TransactionType.GetAccountDetails, TransactionModifier.Additional);
    }

    public ProPayBuilder getAccountBalance() {
        return new ProPayBuilder(TransactionType.GetAccountBalance);
    }

    public ProPayBuilder orderDevice() {
        return new ProPayBuilder(TransactionType.OrderDevice);
    }

    public void Dispose() {
    }

}
