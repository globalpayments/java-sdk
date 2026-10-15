package com.global.api.entities.payFac;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Accessors(chain = true)
@Getter
@Setter
public class ProcessingLimits {
    // Card transaction limit amount
    private BigDecimal cardTransactionLimitAmount;
    // Card monthly limit amount
    private BigDecimal cardMonthlyLimitAmount;
    // Bank transfer transaction limit amount
    private BigDecimal bankTransferTransactionLimitAmount;
    // Bank transfer monthly limit amount
    private BigDecimal bankTransferMonthlyLimitAmount;
    // Disable payout card limit percentage
    private BigDecimal disablePayoutCardLimitPercentage;
    // Disable payout bank transfer limit percentage
    private BigDecimal disablePayoutBankTransferLimitPercentage;
    // Allowed negative balance amount
    private BigDecimal allowedNegativeBalanceAmount;
}

