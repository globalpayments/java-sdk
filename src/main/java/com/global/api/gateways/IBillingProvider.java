package com.global.api.gateways;

import com.global.api.builders.BillingBuilder;
import com.global.api.entities.billing.BillingResponse;
import com.global.api.entities.exceptions.ApiException;

public interface IBillingProvider {
    public boolean isBillDataHosted();
    public BillingResponse processBillingRequest(BillingBuilder builder) throws ApiException;
}
