package com.global.api.entities.enums;

import java.util.EnumSet;
import java.util.Set;

public enum TransactionType implements IFlag {
    Decline,
    Verify,
    Capture,
    Auth,
    Refund,
    Reversal,
    Sale,
    Edit,
    Void,
    AddValue,
    Balance,
    Activate,
    Alias,
    Replace,
    Reward,
    Deactivate,
    BatchClose,
    Create,
    Delete,
    BenefitWithdrawal,
    Fetch,
    Search,
    Hold,
    Release,
    VerifyEnrolled,
    VerifySignature,
    TransferFunds,
    TokenUpdate,
    TokenDelete,
    Confirm,
    InitiateAuthentication,
    DataCollect,
    PreAuthCompletion,
    DccRateLookup,
    Increment,
    Tokenize,
    CashOut,
    SendFile,
    SplitFunds,
    Payment,
    CashAdvance,
    DisputeAcceptance,
    DisputeChallenge,
    CreateAccount,
    EditAccount,
    LoadReversal,
    // In .NET, we have many more enum types
    Reauth,
    Mail,
    PDL,
    UtilityMessage,
    MagnumPDL,
    EmvPdl,
    PosSiteConfiguration,
    PayByLinkUpdate,
    RiskAssess,
    TimeRequest,
    Issue,
    VerifyAuthentication,
    RequestPendingMessages,
    FileAction,
    StoreAndForward,
    ResetPassword,
    RenewAccount,
    UpdateBeneficialOwnership,
    DisownAccount,
    UploadDocumentChargeback,
    UploadDocument,
    ObtainSSOKey,
    UpdateBankAccountOwnership,
    AddFunds,
    SweepFunds,
    AddCardFlashFunds,
    PushMoneyFlashFunds,
    DisburseFunds,
    SpendBack,
    ReverseSplitPay,
    GetAccountDetails,
    GetAccountBalance,
    GetTokenInfo,
    DeleteOpenTab,
    CheckQueryInfo,
    OrderDevice,
    SurchargeEligibilityLookup;

    public long getLongValue() {
        return 1L << this.ordinal();
    }

    public static Set<TransactionType> getSet(long value) {
        EnumSet<TransactionType> flags = EnumSet.noneOf(TransactionType.class);
        for(TransactionType flag : TransactionType.values()) {
            long flagValue = flag.getLongValue();
            if((flagValue & value) == flagValue)
                flags.add(flag);
        }
        return flags;
    }

    public boolean isReversal() {
        return this.equals(TransactionType.Reversal) || this.equals(TransactionType.LoadReversal);
    }
}
