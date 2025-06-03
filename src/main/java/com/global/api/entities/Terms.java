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
    private String id;
    /**
     * Indicates if installment.term.time_unit_number is days, months or years
     */
    private String timeUnit;
    /**
     * Indicates the total number of payments to be made over the course of the installment payment plan.
     */
    private List<Integer> timeUnitNumbers;
}
