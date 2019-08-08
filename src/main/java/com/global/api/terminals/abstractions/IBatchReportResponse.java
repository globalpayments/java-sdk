package com.global.api.terminals.abstractions;

import com.global.api.entities.BatchSummary;
import com.global.api.entities.TransactionSummary;
import com.global.api.terminals.hpa.responses.CardBrandSummary;

import java.util.ArrayList;

public interface IBatchReportResponse extends IDeviceResponse {
    BatchSummary getBatchSummary();
    CardBrandSummary getVisaSummary();
    CardBrandSummary getMastercardSummary();
    CardBrandSummary getAmexSummary();
    CardBrandSummary getDiscoverSummary();
    CardBrandSummary getPaypalSummary();
    ArrayList<TransactionSummary> getTransactionSummaries();
}
