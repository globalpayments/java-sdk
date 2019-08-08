package com.global.api.terminals.hpa.responses;

import com.global.api.entities.BatchSummary;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.abstractions.IBatchReportResponse;
import com.global.api.utils.Element;

import java.util.ArrayList;

public class BatchReportResponse extends SipKvpResponse implements IBatchReportResponse {
    private TransactionSummary lastTransactionSummary;

    private BatchSummary batchSummary;
    private CardBrandSummary visaSummary;
    private CardBrandSummary mastercardSummary;
    private CardBrandSummary amexSummary;
    private CardBrandSummary discoverSummary;
    private CardBrandSummary paypalSummary;
    private ArrayList<TransactionSummary> transactionSummaries;

    public BatchSummary getBatchSummary() {
        return batchSummary;
    }
    public CardBrandSummary getVisaSummary() {
        return visaSummary;
    }
    public CardBrandSummary getMastercardSummary() {
        return mastercardSummary;
    }
    public CardBrandSummary getAmexSummary() {
        return amexSummary;
    }
    public CardBrandSummary getDiscoverSummary() {
        return discoverSummary;
    }
    public CardBrandSummary getPaypalSummary() {
        return paypalSummary;
    }
    public ArrayList<TransactionSummary> getTransactionSummaries() {
        return transactionSummaries;
    }

    BatchReportResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }

    protected void mapResponse(Element response) {
        super.mapResponse(response);

        if(category != null) {
            if(category.equalsIgnoreCase("BATCH DETAIL") || category.equalsIgnoreCase("BATCH SUMMARY")) {
                if(batchSummary == null) {
                    batchSummary = new BatchSummary();
                }

                batchSummary.setMerchantName(fieldValues.getString("MerchantName", batchSummary.getMerchantName()));
                batchSummary.setSiteId(fieldValues.getString("SiteId", batchSummary.getSiteId()));
                batchSummary.setDeviceId(fieldValues.getString("DeviceId", batchSummary.getDeviceId()));
                batchSummary.setBatchId(fieldValues.getInt("BatchId", batchSummary.getBatchId()));
                batchSummary.setSequenceNumber(fieldValues.getString("BatchSeqNbr", batchSummary.getSequenceNumber()));
                batchSummary.setStatus(fieldValues.getString("BatchStatus", batchSummary.getStatus()));
                batchSummary.setOpenTime(fieldValues.getDateTime("OpenUtcDT", batchSummary.getOpenTime()));
                batchSummary.setOpenTransactionId(fieldValues.getString("OpenTxnId", batchSummary.getOpenTransactionId()));
                batchSummary.setCloseTransactionId(fieldValues.getString("CloseTxnId", batchSummary.getCloseTransactionId()));
                batchSummary.setCloseCount(fieldValues.getInt("BatchTxnCnt", batchSummary.getCloseCount()));
                batchSummary.setTotalAmount(fieldValues.getAmount("BatchTxnAmt", batchSummary.getTotalAmount()));
                batchSummary.setCreditCount(fieldValues.getInt("CreditCnt", batchSummary.getCreditCount()));
                batchSummary.setCreditAmount(fieldValues.getAmount("CreditAmt", batchSummary.getCreditAmount()));
                batchSummary.setDebitCount(fieldValues.getInt("DebitCnt", batchSummary.getDebitCount()));
                batchSummary.setDebitAmount(fieldValues.getAmount("DebitAmt", batchSummary.getDebitAmount()));
                batchSummary.setSaleCount(fieldValues.getInt("SaleCnt", batchSummary.getSaleCount()));
                batchSummary.setSaleAmount(fieldValues.getAmount("SaleAmt", batchSummary.getSaleAmount()));
                batchSummary.setReturnCount(fieldValues.getInt("ReturnCtn", batchSummary.getReturnCount()));
                batchSummary.setReturnAmount(fieldValues.getAmount("ReturnAmt", batchSummary.getReturnAmount()));
            }
            if(category.equalsIgnoreCase("VISA CARD SUMMARY") ||
                    category.equalsIgnoreCase("MASTERCARD CARD SUMMARY") ||
                    category.equalsIgnoreCase("AMERICAN EXPRESS CARD SUMMARY") ||
                    category.equalsIgnoreCase("DISCOVER CARD SUMMARY") ||
                    category.equalsIgnoreCase("PAYPAL CARD SUMMARY")) {
                try {
                    CardBrandSummary brandSummary = new CardBrandSummary(currentMessage.getBytes(), "GetBatchReport");
                    if(category.equalsIgnoreCase("VISA CARD SUMMARY")) { visaSummary = brandSummary; }
                    if(category.equalsIgnoreCase("MASTERCARD CARD SUMMARY")) { mastercardSummary = brandSummary; }
                    if(category.equalsIgnoreCase("AMERICAN EXPRESS CARD SUMMARY")) { amexSummary = brandSummary; }
                    if(category.equalsIgnoreCase("DISCOVERY CARD SUMMARY")) { discoverSummary = brandSummary; }
                    if(category.equalsIgnoreCase("PAYPAL CARD SUMMARY")) { paypalSummary = brandSummary; }
                }
                catch(ApiException exc) { /* NOM NOM */ }
            }
            if(category.equalsIgnoreCase("TRANSACTION # DETAIL")) {
                TransactionSummary summary = new TransactionSummary();
                if(category.equalsIgnoreCase(lastCategory)) {
                    summary = lastTransactionSummary;
                }

                summary.setReferenceNumber(fieldValues.getString("ReferenceNumber", summary.getReferenceNumber()));
                summary.setTransactionDate(fieldValues.getDateTime("TransactionTime", summary.getTransactionDate()));
                summary.setTransactionStatus(fieldValues.getString("TransactionStatus", summary.getTransactionStatus()));
                summary.setMaskedCardNumber(fieldValues.getString("MaskedPAN", summary.getMaskedCardNumber()));
                summary.setCardType(fieldValues.getString("CardType", summary.getCardType()));
                summary.setTransactionType(fieldValues.getString("TransactionType", summary.getTransactionType()));
                summary.setCardEntryMethod(fieldValues.getString("CardAcquisition", summary.getCardEntryMethod()));
                summary.setAuthCode(fieldValues.getString("ApprovalCode", summary.getAuthCode()));
                summary.setGatewayResponseCode(fieldValues.getString("Responsecode", summary.getGatewayResponseCode()));
                summary.setGatewayResponseMessage(fieldValues.getString("ResponseText", summary.getGatewayResponseMessage()));
                summary.setCashBackAmount(fieldValues.getAmount("CashbackAmount", summary.getCashBackAmount()));
                summary.setGratuityAmount(fieldValues.getAmount("TipAmount", summary.getGratuityAmount()));
                summary.setAuthorizedAmount(fieldValues.getAmount("AuthorizedAmount", summary.getAuthorizedAmount()));
                summary.setSettlementAmount(fieldValues.getAmount("SettleAmount", summary.getSettlementAmount()));
                summary.setAmount(fieldValues.getAmount("RequestedAmount", summary.getAmount()));

                if(!category.equalsIgnoreCase(lastCategory)) {
                    if(transactionSummaries == null) {
                        transactionSummaries = new ArrayList<TransactionSummary>();
                    }
                    transactionSummaries.add(summary);
                }
                lastTransactionSummary = summary;
            }
            lastCategory = category;
        }
    }
}
