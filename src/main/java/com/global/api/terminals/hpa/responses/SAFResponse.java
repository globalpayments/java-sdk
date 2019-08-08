package com.global.api.terminals.hpa.responses;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.global.api.entities.TransactionSummary;
import com.global.api.entities.enums.SAFReportType;
import com.global.api.entities.enums.SummaryType;
import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.SummaryResponse;
import com.global.api.terminals.abstractions.ISAFResponse;
import com.global.api.utils.*;

public class SAFResponse extends SipBaseResponse implements ISAFResponse {
    private BigDecimal totalAmount;
    private Integer totalCount;
    private Map<SummaryType, SummaryResponse> approved;
    private Map<SummaryType, SummaryResponse> pending;
    private Map<SummaryType, SummaryResponse> declined;

    private String lastCategory;
    private TransactionSummary lastTransactionSummary;

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public Integer getTotalCount() {
        return totalCount;
    }
    public Map<SummaryType, SummaryResponse> getApproved() {
        return approved;
    }
    public Map<SummaryType, SummaryResponse> getPending() {
        return pending;
    }
    public Map<SummaryType, SummaryResponse> getDeclined() {
        return declined;
    }

    SAFResponse(byte[] buffer, String... messageIds) throws ApiException {
        super(buffer, messageIds);
    }

    protected void mapResponse(Element response) {
        super.mapResponse(response);
        
        // set category
        String category = response.getString("TableCategory");
        if(category == null) {
            category = lastCategory;
        }

        // build the response dictionary
        if(category != null) {
            VariableDictionary fieldValues = new VariableDictionary();
            for (Element field : response.getAll("Field")) {
                String key = field.getString("Key");
                String value = field.getString("Value");
                fieldValues.put(key, value);
            }

            if (category.endsWith("SUMMARY")) {
                SummaryResponse summary = new SummaryResponse();
                summary.setSummaryType(mapSummaryType(category));
                summary.setCount(fieldValues.getInt("Count"));
                summary.setAmount(fieldValues.getAmount("Amount"));
                summary.setTotalAmount(fieldValues.getAmount("Total Amount"));
                summary.setAuthorizedAmount(fieldValues.getAmount("Authorized Amount"));
                summary.setAmountDue(fieldValues.getAmount("Balance Due Amount"));

                if (category.contains("APPROVED")) {
                    if (approved == null) {
                        approved = new HashMap<SummaryType, SummaryResponse>();
                    }
                    approved.put(summary.summaryType, summary);
                } else if (category.startsWith("PENDING")) {
                    if (pending == null) {
                        pending = new HashMap<SummaryType, SummaryResponse>();
                    }
                    pending.put(summary.summaryType, summary);
                } else if (category.startsWith("DECLINED")) {
                    if (declined == null) {
                        declined = new HashMap<SummaryType, SummaryResponse>();
                    }
                    declined.put(summary.summaryType, summary);
                }
            }
            else if (category.endsWith("RECORD")) {
                TransactionSummary trans;

                if (category.equalsIgnoreCase(lastCategory)) {
                    trans = lastTransactionSummary;
                } else {
                    trans = new TransactionSummary();
                }

                if (fieldValues.containsKey("TransactionId")) {
                    trans.setTransactionId(fieldValues.getString("TransactionId"));
                }
                if (fieldValues.containsKey("TransactionId")) {
                    trans.setOriginalTransactionId(fieldValues.getString("TransactionId"));
                }
                if (fieldValues.containsKey("TransactionTime")) {
                    trans.setTransactionDate(fieldValues.getDateTime("TransactionTime"));
                }
                if (fieldValues.containsKey("TransactionType")) {
                    trans.setTransactionType(fieldValues.getString("TransactionType"));
                }
                if (fieldValues.containsKey("MaskedPAN")) {
                    trans.setMaskedCardNumber(fieldValues.getString("MaskedPAN"));
                }
                if (fieldValues.containsKey("CardType")) {
                    trans.setCardType(fieldValues.getString("CardType"));
                }
                if (fieldValues.containsKey("CardAcquisition")) {
                    trans.setCardEntryMethod(fieldValues.getString("CardAcquisition"));
                }
                if (fieldValues.containsKey("ApprovalCode")) {
                    trans.setAuthCode(fieldValues.getString("ApprovalCode"));
                }
                if (fieldValues.containsKey("ResponseCode")) {
                    trans.setIssuerResponseCode(fieldValues.getString("ResponseCode"));
                }
                if (fieldValues.containsKey("ResponseText")) {
                    trans.setIssuerResponseMessage(fieldValues.getString("ResponseText"));
                }
                if (fieldValues.containsKey("HostTimeOut")) {
                    trans.setHostTimeOut(fieldValues.getBoolean("HostTimeOut"));
                }
                if (fieldValues.containsKey("TaxAmount")) {
                    trans.setTaxAmount(fieldValues.getAmount("TaxAmount"));
                }
                if (fieldValues.containsKey("TipAmount")) {
                    trans.setGratuityAmount(fieldValues.getAmount("TipAmount"));
                }
                if (fieldValues.containsKey("RequestAmount")) {
                    trans.setAmount(fieldValues.getAmount("RequestAmount"));
                }
                if (fieldValues.containsKey("Authorized Amount")) {
                    trans.setAuthorizedAmount(fieldValues.getAmount("Authorized Amount"));
                }
                if (fieldValues.containsKey("Balance Due Amount")) {
                    trans.setAmountDue(fieldValues.getAmount("Balance Due Amount"));
                }

                if (!category.equalsIgnoreCase(lastCategory)) {
                    if (category.startsWith("APPROVED")) {
                        SummaryResponse summary = approved.get(SummaryType.Approved);
                        summary.transactions.add(trans);
                    } else if (category.startsWith("PENDING")) {
                        SummaryResponse summary = pending.get(SummaryType.Pending);
                        summary.transactions.add(trans);
                    } else if (category.startsWith("DECLINED")) {
                        SummaryResponse summary = declined.get(SummaryType.Declined);
                        summary.transactions.add(trans);
                    }
                }

                lastTransactionSummary = trans;
            }
            lastCategory = category;
        }
    }

    private SummaryType mapSummaryType(String category) {
        if (category.equalsIgnoreCase(SAFReportType.APPROVED.getValue())) {
            return SummaryType.Approved;
        }
        else if (category.equalsIgnoreCase(SAFReportType.PENDING.getValue())) {
            return SummaryType.Pending;
        }
        else if (category.equalsIgnoreCase(SAFReportType.DECLINED.getValue())) {
            return SummaryType.Declined;
        }
        else if(category.equalsIgnoreCase(SAFReportType.OFFLINE_APPROVED.getValue())) {
            return SummaryType.OfflineApproved;
        }
        else if (category.equalsIgnoreCase(SAFReportType.PARTIALLY_APPROVED.getValue())) {
            return SummaryType.PartiallyApproved;
        }
        else if (category.equalsIgnoreCase(SAFReportType.APPROVED_VOID.getValue())) {
            return SummaryType.VoidApproved;
        }
        else if (category.equalsIgnoreCase(SAFReportType.PENDING_VOID.getValue())) {
            return SummaryType.VoidPending;
        }
        else if (category.equalsIgnoreCase(SAFReportType.DECLINED_VOID.getValue())) {
            return SummaryType.VoidDeclined;
        }
        return null;
    }

}
