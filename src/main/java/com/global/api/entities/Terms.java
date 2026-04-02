package com.global.api.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Terms {
    /**
     * Represents the reference to the installment option being offered.
     * This field is applicable only if the installment.getProgram() is SIP.
     */
    private String name;

    private String reference;

    private String mode;

    private String count;

    private String gracePeriodCount;

    private String currency;

    private String timeUnit;

    private String costPercentage;

    private String totalPlanCost;

    private String planAmount;

    private String id;

    private List<Integer> timeUnitNumbers;

    private String maxTimeUnitNumber;

    private String maxAmount;

    private String language;

    private String version;

    private Fees fees;

    private List<TermsAndConditions> termsAndConditions;

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public boolean isEmpty() {
        return isNullOrEmpty(id) &&
                isNullOrEmpty(timeUnit) &&
                (timeUnitNumbers == null || timeUnitNumbers.isEmpty()) &&
                isNullOrEmpty(maxTimeUnitNumber);
    }

}
