package com.global.api.services;

import com.global.api.ServicesContainer;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.gateways.IPaymentGateway;
import com.global.api.network.NetworkMessageHeader;
import com.global.api.builders.ResubmitBuilder;
import com.global.api.entities.enums.TransactionType;

public class NetworkService {
    public static ResubmitBuilder resubmitBatchClose(String transactionToken) {
        return new ResubmitBuilder(TransactionType.BatchClose)
                .withTransactionToken(transactionToken);
    }

    public static ResubmitBuilder resubmitDataCollect(String transactionToken) {
        return resubmitDataCollect(transactionToken, false);
    }
    public static ResubmitBuilder resubmitDataCollect(String transactionToken, boolean forced) {
        return new ResubmitBuilder(TransactionType.DataCollect)
                .withTransactionToken(transactionToken)
                .withForceToHost(forced);
    }

    public static ResubmitBuilder forcedRefund(String transactionToken) {
        return new ResubmitBuilder(TransactionType.Refund)
                .withTransactionToken(transactionToken)
                .withForceToHost(true);
    }

    public static ResubmitBuilder forcedSale(String transactionToken) {
        return new ResubmitBuilder(TransactionType.Sale)
                .withTransactionToken(transactionToken)
                .withForceToHost(true);
    }
  
    public static NetworkMessageHeader sendKeepAlive() throws ApiException {
        return sendKeepAlive("default");
    }
    
    public static NetworkMessageHeader sendKeepAlive(String configName) throws ApiException {
    	  IPaymentGateway gateway = ServicesContainer.getInstance().getGateway(configName);
        return gateway.sendKeepAlive();
        
    }
}
