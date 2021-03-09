package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.billing.BillingResponse;

public class BillingRequestResponse extends BillPayResponseBase<BillingResponse> {
    public BillingResponse map() {
        BillingResponse result = new BillingResponse();
        
        result.setIsSuccessful(response.getBool("a:isSuccessful"));
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        
        return result;
    }
}
