package com.global.api.paymentMethods;

import lombok.Getter;
import lombok.Setter;

public class InstallmentData  {
    //Indicates the installment payment plan program.
    @Getter @Setter private String program;
    //Indicates the mode of the Installment plan choosen
    @Getter @Setter private String mode;
    //Indicates the total number of payments to be made over the course of the installment payment plan.
    @Getter @Setter private String count;
    //Indicates the grace period before the first payment.
    @Getter @Setter private String gracePeriodCount;

}