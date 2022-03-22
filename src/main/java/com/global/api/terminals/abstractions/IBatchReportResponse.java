package com.global.api.terminals.abstractions;

import com.global.api.entities.BatchSummary;
import com.global.api.entities.TransactionSummary;
import com.global.api.terminals.hpa.responses.CardBrandSummary;

import java.util.ArrayList;

public interface IBatchReportResponse extends IDeviceResponse {
    BatchSummary getBatchSummary();
    ICardBrandSummary getVisaSummary();
    ICardBrandSummary getMastercardSummary();
    ICardBrandSummary getAmexSummary();
    ICardBrandSummary getDiscoverSummary();
    ICardBrandSummary getPaypalSummary();
    ArrayList<TransactionSummary> getTransactionSummaries();
}
