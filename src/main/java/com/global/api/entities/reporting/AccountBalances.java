package com.global.api.entities.reporting;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Represents account balance information for FMA (Fund Management Account) details
 * Includes available balance, pending balance, and reserve balance
 */
@Accessors(chain = true)
@Getter
@Setter
public class AccountBalances {
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private BigDecimal reserveBalance;
}

