package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a collection of fee information and related total amounts.
 */
@Getter @Setter
public class Fees {
    /**
     * List of fee information details.
     */
    private List<FeeInfo> feeInfo;

    /**
     * The total amount of all fees.
     */
    private String totalAmount;

    /**
     * The total amount for subsequent fees.
     */
    private String totalSubsequentAmount;

    /**
     * The amount for a single subsequent fee.
     */
    private String subsequentAmount;

    /**
     * The total amount for upfront fees.
     */
    private String totalUpfrontAmount;

    /**
     * The amount for a single upfront fee.
     */
    private String upfrontAmount;
}
