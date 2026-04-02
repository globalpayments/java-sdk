package com.global.api.paymentMethods;

import com.global.api.entities.Terms;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class InstallmentData  {
    //Indicates the installment payment plan program.
    @Getter @Setter private String program;
    //Indicates the mode of the Installment plan choosen
    @Getter @Setter private String mode;
    //Indicates the total number of payments to be made over the course of the installment payment plan.
    @Getter @Setter private String count;
    //Indicates the grace period before the first payment.
    @Getter @Setter private String gracePeriodCount;

    //Installment ID reference (from installment query)
    @Getter @Setter private String reference;

    //Visa installment funding mode (e.g., 'MERCHANT', 'ISSUER')
    @Getter @Setter private String fundingMode;

    //Visa installment terms
    @Getter @Setter private Terms terms;

    //Array of eligible plans for Visa installments
    @Getter @Setter private List<String> eligiblePlans;

    @Getter @Setter private String id;

}
