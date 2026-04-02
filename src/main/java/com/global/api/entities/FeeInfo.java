package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
/**
 * Represents fee-related information associated with a transaction or account.
 * <p>
 * This class captures the type of fee, a percentage-based component, and a
 * flat (fixed) monetary component. All values are stored as strings to allow
 * flexible formatting and to avoid precision issues in serialization.
 */
public class FeeInfo {
    /**
     * The fee type identifier or description.
     * <p>
     * This may be a code or human-readable label indicating the nature of the fee,
     * for example {@code "SERVICE_FEE"}, {@code "LATE_FEE"}, or {@code "INTERCHANGE"}.
     */
    private String type;

    /**
     * Interest or percentage rate component of the fee, expressed as a decimal string.
     * <p>
     * The value typically represents a percentage rate with up to two decimal places,
     * for example {@code "5.00"} for a 5% rate or {@code "0.50"} for a 0.5% rate.
     * The exact interpretation (annual, monthly, etc.) is defined by the calling context.
     */
    private String interestRate;

    /**
     * Flat (fixed) monetary amount component of the fee, expressed as a decimal string.
     * <p>
     * The value typically represents a currency amount with two decimal places,
     * for example {@code "10.00"} for a fee of 10 units of the applicable currency.
     * Currency is determined by the surrounding transaction or account context.
     */
    private String flatAmount;
}
