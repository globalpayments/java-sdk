package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.billing.BillingResponse;

public class CommitPreloadBillsResponse extends BillPayResponseBase<BillingResponse> {
    public BillingResponse map() {
        BillingResponse result = new BillingResponse();
        
        result.setIsSuccessful(response.getBool("a:isSuccessful"));
        result.setResponseCode(getFirstResponseCode(response));
        result.setResponseMessage(getFirstResponseMessage(response));
        
        return result;
    }
}
