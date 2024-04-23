package com.global.api.services;

import com.global.api.builders.SurchargeEligibilityBuilder;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.reporting.SurchargeLookup;

public class SurchargeEligibilityService {

    public static SurchargeEligibilityBuilder<SurchargeLookup> eligibilityLookup() {
        return new SurchargeEligibilityBuilder<>(TransactionType.SurchargeEligibilityLookup);
    }
}
