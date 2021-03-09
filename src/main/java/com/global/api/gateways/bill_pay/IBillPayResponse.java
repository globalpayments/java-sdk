package com.global.api.gateways.bill_pay;

import com.global.api.entities.exceptions.ApiException;

public interface IBillPayResponse<T> {
    T map();
    IBillPayResponse<T> withResponseTagName(String tagName);
    IBillPayResponse<T> withResponse(String response) throws ApiException;
}
