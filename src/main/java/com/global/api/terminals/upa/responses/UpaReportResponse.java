package com.global.api.terminals.upa.responses;

import com.global.api.entities.BatchSummary;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.exceptions.GatewayException;
import com.global.api.terminals.abstractions.IBatchReportResponse;
import com.global.api.terminals.abstractions.ICardBrandSummary;
import com.global.api.utils.JsonDoc;

import java.math.BigDecimal;
import java.util.ArrayList;

public class UpaReportResponse implements IBatchReportResponse {
    private UpaCardBrandSummary amexSummary;
    private BatchSummary batchSummary;
    private String deviceResponseCode;
    private String deviceResponseText;
    private UpaCardBrandSummary debitSummary;
    private UpaCardBrandSummary discoverSummary;
    private UpaCardBrandSummary mastercardSummary;
    private String status;
    private ArrayList<TransactionSummary> transactions = new ArrayList<>();
    private String transactionType;
    private UpaCardBrandSummary visaSummary;

    public UpaReportResponse(JsonDoc responseObj) {
        JsonDoc responseData = responseObj.get("data");

        if (responseData != null) {
            JsonDoc cmdResult = responseData.get("cmdResult");

            if (cmdResult != null) {
                status = cmdResult.getString("result");
                deviceResponseCode = status.equalsIgnoreCase("success") ? "00" : cmdResult.getString("errorCode");
                deviceResponseText = cmdResult.getString("errorMessage");
            }

            transactionType = responseData.getString("response");

            JsonDoc innerData = responseData.get("data");

            if (innerData != null) {
                JsonDoc batchRecord = innerData.get("batchRecord");

                if (batchRecord != null) {
                    batchSummary = new BatchSummary();
                    batchSummary.setBatchId(batchRecord.getInt("batchId"));
                    batchSummary.setSequenceNumber(batchRecord.getString("batchSeqNbr"));
                    batchSummary.setStatus(batchRecord.getString("batchStatus"));
                    try {
                        batchSummary.setOpenTime(batchRecord.getDateTime("openUtcDateTime"));
                    } catch (GatewayException e) {
                        e.printStackTrace();
                    }
                    batchSummary.setOpenTransactionId(batchRecord.getString("openTxnId"));
                    batchSummary.setOpenTransactionId(batchRecord.getString("openTnxId")); // to account for current
                    // typo in UPA
                    batchSummary.setTotalAmount(batchRecord.getDecimal("totalAmount"));
                    batchSummary.setTransactionCount(batchRecord.getInt("totalCnt"));

                    ArrayList batchDetailRecords = batchRecord.getStringArrayList("batchDetailRecords");

                    if (batchDetailRecords != null) {
                        batchDetailRecords.forEach((n) -> {
                            TransactionSummary trans = new TransactionSummary();
                            trans.setAmountDue(((JsonDoc) n).getDecimal("balanceDue"));
                            trans.setAuthCode(((JsonDoc) n).getString("approvalCode"));
                            trans.setAuthorizedAmount(((JsonDoc) n).getDecimal("authorizedAmount"));
                            trans.setBaseAmount(((JsonDoc) n).getDecimal("baseAmount"));
                            trans.setCardSwiped(((JsonDoc) n).getString("cardSwiped"));
                            trans.setCardType(((JsonDoc) n).getString("cardType"));
                            trans.setCashBackAmount(((JsonDoc) n).getDecimal("cashbackAmount"));
                            trans.setClerkId(((JsonDoc) n).getString("clerkId"));
                            trans.setInvoiceNumber(((JsonDoc) n).getString("invoiceNbr"));
                            trans.setMaskedCardNumber(((JsonDoc) n).getString("maskedCardNumber"));
                            trans.setSettlementAmount(((JsonDoc) n).getDecimal("settleAmount"));
                            trans.setTaxAmount(((JsonDoc) n).getDecimal("taxAmount"));
                            trans.setGratuityAmount(((JsonDoc) n).getDecimal("tipAmount"));
                            trans.setAmount(((JsonDoc) n).getDecimal("totalAmount"));
                            trans.setTransactionId(((JsonDoc) n).getString("gatewayTxnId"));
                            trans.setTransactionStatus(((JsonDoc) n).getString("transactionStatus"));
                            trans.setTransactionType(((JsonDoc) n).getString("transactionType"));

                            transactions.add(trans);
                        });
                    }

                    ArrayList batchCardSummary = batchRecord.getStringArrayList("batchTransactions");

                    if (batchCardSummary != null) {
                        batchCardSummary.forEach((n) -> {
                            JsonDoc record = (JsonDoc) n;
                            switch (record.getString("cardType").toUpperCase()) {
                                case "AMEX":
                                case "AMERICAN EXPRESS":
                                    amexSummary = new UpaCardBrandSummary(record);
                                    return;
                                case "DEBIT":
                                    debitSummary = new UpaCardBrandSummary(record);
                                    return;
                                case "DISCOVER":
                                    discoverSummary = new UpaCardBrandSummary(record);
                                    return;
                                case "MC":
                                case "MASTERCARD":
                                    mastercardSummary = new UpaCardBrandSummary(record);
                                    return;
                                case "VISA":
                                    visaSummary = new UpaCardBrandSummary(record);
                                    return;
                                default:
                                    return;
                            }
                        });
                    }
                }

                ArrayList openTabDetails = innerData.getStringArrayList("OpenTabDetails");

                if (openTabDetails != null) {
                    openTabDetails.forEach((n) -> {
                        TransactionSummary trans = new TransactionSummary();
                        trans.setAuthorizedAmount(((JsonDoc) n).getDecimal("authorizedAmount"));
                        trans.setCardType(((JsonDoc) n).getString("cardType"));
                        trans.setClerkId(((JsonDoc) n).getString("clerkId"));
                        trans.setMaskedCardNumber(((JsonDoc) n).getString("maskedPan"));
                        trans.setTransactionId(((JsonDoc) n).getString("referenceNumber"));

                        transactions.add(trans);
                    });
                }
            }
        }
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getStatus() {
        return status;
    }

    public String getDeviceResponseCode() {
        return deviceResponseCode;
    }

    public String getDeviceResponseText() {
        return deviceResponseText;
    }

    public ICardBrandSummary getVisaSummary() {
        return visaSummary;
    }

    public ICardBrandSummary getMastercardSummary() {
        return mastercardSummary;
    }

    public ICardBrandSummary getAmexSummary() {
        return amexSummary;
    }

    public ICardBrandSummary getDiscoverSummary() {
        return discoverSummary;
    }

    public ICardBrandSummary getDebitSummary() {
        return debitSummary;
    }

    public ICardBrandSummary getPaypalSummary() {
        return null;
    }

    public ArrayList<TransactionSummary> getTransactionSummaries() {
        return transactions;
    }

    public BatchSummary getBatchSummary() {
        return batchSummary;
    }

    public void setDeviceResponseCode(String deviceResponseCode) {
        // Unused
    }

    public void setDeviceResponseText(String deviceResponseMessage) {
        // Unused
    }

    public String getVersion() {
        return null;
    }

    public void setVersion(String version) {
        // Unused
    }

    public void setStatus(String status) {
        // Unused
    }

    public String getCommand() {
        return null;
    }

    public void setCommand(String command) {
        // Unused
    }
}
