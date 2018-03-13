package com.global.api.services;

import com.global.api.builders.ManagementBuilder;
import com.global.api.entities.BatchSummary;
import com.global.api.entities.Transaction;
import com.global.api.entities.enums.TransactionType;
import com.global.api.entities.exceptions.ApiException;

public class BatchService {
    public static BatchSummary closeBatch() throws ApiException {
        Transaction response = new ManagementBuilder(TransactionType.BatchClose).execute();
        return response.getBatchSummary();
    }
}
