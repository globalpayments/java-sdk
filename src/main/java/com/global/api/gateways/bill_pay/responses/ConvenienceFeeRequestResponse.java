package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.billing.ConvenienceFeeResponse;

public class ConvenienceFeeRequestResponse extends BillPayResponseBase<ConvenienceFeeResponse> {
    public ConvenienceFeeResponse map() {
        ConvenienceFeeResponse result = new ConvenienceFeeResponse();
        
        result.setIsSuccessful(response.getBool("a:isSuccessful"));
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        result.setConvenienceFee(response.getDecimal("a:ConvenienceFee"));
        
        return result;
    }
}
