package com.global.api.entities.propay;

import com.global.api.entities.enums.ProPayAccountStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountPermissions {

    /** Account permitted to load funds via ACH. Valid values are: Y and N */
    private boolean isACHIn;

    /** Account balance allowed to be pushed to on-file DDA. Affects automatic sweeps. Valid values are: Y and N */
    private boolean isACHOut;

    /** Valid values are: Y and N */
    private boolean isCCProcessing;

    /** Valid values are: Y and N */
    private boolean isProPayIn;

    /** Valid values are: Y and N */
    private boolean isProPayOut;

    /** Valid values between 0 and 999999999. Expressed as number of pennies in USD or number of account's currency without decimals */
    private String creditCardMonthLimit;

    /** Valid values between 0 and 999999999. Expressed as number of pennies in USD or number of account's currency without decimals */
    private String creditCardTransactionLimit;

    /** Used to updated status of ProPay account. Note: the ONLY value that will allow an account to process transactions is "ReadyToProcess" */
    private ProPayAccountStatus merchantOverallStatus;

    /** Valid values are Y and N. Please work with ProPay for more information about soft limits feature */
    private boolean isSoftLimitEnabled;

    /** Valid values are Y and N. Please work with ProPay for more information about soft limits feature */
    private boolean isACHPaymentSoftLimitEnabled;

    /** Valid values between 0 and 499. Please work with ProPay for more information about soft limits feature */
    private String softLimitACHOffPercent;

    /** Valid values between 0 and 499. Please work with ProPay for more information about soft limits feature */
    private String achPaymentACHOffPercent;

    private String achPaymentMonthLimit;

    private String achPaymentPerTranLimit;
    
}
