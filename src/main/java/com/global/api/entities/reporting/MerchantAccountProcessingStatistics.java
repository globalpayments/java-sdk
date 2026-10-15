package com.global.api.entities.reporting;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MerchantAccountProcessingStatistics {
    private BigDecimal cardTransactionLimitAmount;
    private BigDecimal cardMonthlyLimitAmount;
    private BigDecimal bankTransferPerTransactionLimitAmount;
    private BigDecimal bankTransferMonthlyLimitAmount;
    private BigDecimal bankTransferMonthToDateProcessedAmount;
    private BigDecimal cardMonthToDateProcessedAmount;
    private BigDecimal availableBalanceAmount;
    private BigDecimal pendingBalanceAmount;
    private BigDecimal reserveBalanceAmount;
    private BigDecimal allowedNegativeBalanceAmount;
    private BigDecimal disablePayoutCardLimitPercentage;
    private BigDecimal disablePayoutBankTransferLimitPercentage;
    private BigDecimal cardMonthToDateProcessedPercentage;
    private BigDecimal bankTransferMonthToDateProcessedPercentage;
}