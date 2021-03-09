package com.global.api.gateways.bill_pay;

import com.global.api.entities.billing.Credentials;
import com.global.api.utils.ElementTree;

public interface IBillPayRequest<T, U> {
    IBillPayRequest<T, U> build(ElementTree et, T builder, Credentials credentials);
    public IBillPayResponse<U> execute();
    public IBillPayResponse<U> execute(String endpoint);
}
