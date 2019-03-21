package com.global.api.network.abstractions;

import com.global.api.entities.enums.PaymentMethodType;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.BatchFullException;
import com.global.api.network.entities.PriorMessageInformation;
import com.global.api.utils.IRequestEncoder;

import java.math.BigDecimal;
import java.util.LinkedList;

public interface IBatchProvider {
    int getBatchNumber();
    int getSequenceNumber() throws BatchFullException;
    int getTransactionCount();
    BigDecimal getTotalCredits();
    BigDecimal getTotalDebits();
    IRequestEncoder getRequestEncoder();
    LinkedList<String> getEncodedRequests();
    PriorMessageInformation getPriorMessageData();
    void setPriorMessageData(PriorMessageInformation value);

    void reportDataCollect(TransactionType transactionType, PaymentMethodType paymentMethodType, BigDecimal amount, String encodedRequest);
    void closeBatch(boolean inBalance);
}
