package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.billing.TokenResponse;

public class CreateCustomerAccountResponse extends BillPayResponseBase<TokenResponse> {
    public TokenResponse map() {
        TokenResponse result = new TokenResponse();
        
        result.setIsSuccessful(response.getBool("a:isSuccessful"));
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        result.setToken(response.getString("a:Token"));
        
        return result;
    }
}
