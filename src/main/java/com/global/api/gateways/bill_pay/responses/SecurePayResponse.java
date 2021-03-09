package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.billing.LoadSecurePayResponse;

public class SecurePayResponse extends BillPayResponseBase<LoadSecurePayResponse> {
    public LoadSecurePayResponse map() {
        LoadSecurePayResponse result = new LoadSecurePayResponse();
        
        result.setPaymentIdentifier(response.getString("a:GUID"));
        result.setIsSuccessful(response.getBool("a:isSuccessful"));
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        
        return result;
    }
}
