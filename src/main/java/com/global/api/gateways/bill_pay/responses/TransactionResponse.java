package com.global.api.gateways.bill_pay.responses;

import com.global.api.entities.Transaction;

public class TransactionResponse extends BillPayResponseBase<Transaction> {
    public Transaction map() {
        Transaction result = new Transaction();
        
        result.setResponseCode(response.getString("a:ResponseCode"));
        result.setResponseMessage(getFirstResponseMessage(response));
        result.setAvsResponseCode(response.getString("a:AvsResponseCode"));
        result.setAvsResponseMessage(response.getString("a:AvsResponseText"));
        result.setCvnResponseCode(response.getString("a:CvvResponseCode"));
        result.setCvnResponseMessage(response.getString("a:CvvResponseText"));
        result.setClientTransactionId(response.getString("a:MerchantTransactionID"));
        result.setTimestamp(response.getString("a:TransactionDate"));
        result.setTransactionId(response.getString("a:Transaction_ID"));
        result.setReferenceNumber(response.getString("a:ReferenceTransactionID"));
        result.setToken(response.getString("a:Token"));
        result.setConvenienceFee(response.getDecimal("a:ConvenienceFee"));
        
        return result;
    }
}
