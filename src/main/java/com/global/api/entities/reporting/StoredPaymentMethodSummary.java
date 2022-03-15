package com.global.api.entities.reporting;

import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

public class StoredPaymentMethodSummary {

    @Getter @Setter private String id;
    @Getter @Setter private DateTime timeCreated;
    @Getter @Setter private String status;
    @Getter @Setter private String reference;
    @Getter @Setter private String name;
    @Getter @Setter private String cardLast4;
    @Getter @Setter private String cardType;
    @Getter @Setter private String cardExpMonth;
    @Getter @Setter private String cardExpYear;

}