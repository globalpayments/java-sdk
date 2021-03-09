package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.Transaction;
import com.global.api.utils.Element;

public class ReversalResponse extends BillPayResponseBase<Transaction> {
    public Transaction map() {
        Element authorizationElement = response.get("a:ReversalTransactionWithReversalAuthorizations");
        Transaction result = new Transaction();
        
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        result.setClientTransactionId(authorizationElement.getString("a:MerchantTransactionID"));
        result.setTimestamp(authorizationElement.getString("a:TransactionDate"));
        result.setTransactionId(authorizationElement.getString("a:TransactionID"));
        result.setReferenceNumber(authorizationElement.getString("a:ReferenceTransactionID"));
        
        return result;
    }
}
