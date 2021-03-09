package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.Transaction;

public class TokenRequestResponse extends BillPayResponseBase<Transaction> {
    public Transaction map() {
        Transaction result = new Transaction();
        
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        result.setToken(response.getString("a:Token"));
        
        return result;
    }
}
