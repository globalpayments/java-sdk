package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.Transaction;

public class UpdateTokenResponse extends BillPayResponseBase<Transaction> {
    public Transaction map() {
        Transaction result = new Transaction();
        
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        
        return result;
    }
}
